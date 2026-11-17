const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_ghost_1";
const BOB_UID = "bob_ghost_2";
const CHARLIE_UID = "charlie_ghost_3";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    gamertag: "GhostPilot",
    preferredPlatform: "XBOX_CLOUD",
    preferredQuality: "1080P_60FPS",
    preferredRegion: "NA_EAST",
    totalPlayMinutes: 0,
    cloudPassTier: "FREE_UNLIMITED",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validCloudSavePayload(uid, ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    gameId: "need_for_speed",
    gameTitle: "Need for Speed",
    slotName: "Main Street Garage",
    chapterOrRank: "Rep Lv. 14 - Palm City Night",
    progressPercent: 45,
    playTimeMinutes: 120,
    platformOrigin: "XBOX_CLOUD",
    syncStatus: "SYNCED",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validLibraryPayload(uid, gameId = "forza_horizon_6", ts = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    gameId,
    gameTitle: "Forza Horizon 6",
    pinned: true,
    playTimeMinutes: 30,
    lastSessionQuality: "4K_HDR_60FPS",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validLobbyPayload(hostUid, visibility = "PUBLIC", ts = new Date(Date.now() - 2000)) {
  return {
    hostUid,
    hostGamertag: "GhostHost",
    gameId: "cod_warzone",
    gameTitle: "Call of Duty: Warzone",
    title: "Crossplay Quads - Free Cloud",
    region: "NA_EAST",
    crossPlayPlatforms: ["XBOX", "PC", "CLOUD"],
    visibility,
    status: "OPEN",
    maxPlayers: 4,
    memberUids: [hostUid],
    readyUids: [],
    createdAt: ts,
    updatedAt: ts,
  };
}

// 1. Unauthenticated bounds
test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cloud_saves").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("library").get());
  await assertFails(unauthDb.collection("lobbies").where("visibility", "==", "PUBLIC").get());
  await assertFails(unauthDb.collection("lobbies").doc("lobby_1").set(validLobbyPayload(ALICE_UID)));
});

// 2. GamerProfile owner access, updates, shadow fields, future timestamps & cross-user isolation
test("GamerProfile: owner CRUD succeeds, immutable fields & shadow keys rejected, cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update allowed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      gamertag: "GhostShadow99",
      preferredQuality: "4K_HDR_60FPS",
      totalPlayMinutes: 65,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject mutating cloudPassTier or createdAt
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      cloudPassTier: "PAID_ADMIN",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject shadow/ghost field or @DocumentId 'id' key
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validProfilePayload(BOB_UID),
      isAdmin: true,
    })
  );
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validProfilePayload(BOB_UID),
      id: BOB_UID,
    })
  );

  // Reject future timestamp
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(
      validProfilePayload(BOB_UID, new Date(Date.now() + 60000))
    )
  );

  // Cross-user read & write denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ gamertag: "Hijack" }));
  await assertFails(aliceDb.collection("users").get());
});

// 3. CloudSave subcollection: Kotlin-mirrored create, sync update, list & isolation
test("CloudSaves: owner list/create/sync-update/delete succeeds; cross-user & invalid payloads fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Empty subcollection list must succeed (no existing() access in allow list)
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("cloud_saves").get());

  // Create Need for Speed cloud save
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").set(
      validCloudSavePayload(ALICE_UID, created)
    )
  );

  // Sync progress partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").update({
      progressPercent: 55,
      playTimeMinutes: 140,
      chapterOrRank: "Rep Lv. 16 - Heat Level 4",
      syncStatus: "SYNCED",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject progress > 100
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").update({
      progressPercent: 150,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject mutating gameId
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").update({
      gameId: "gta_v",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user list, get, update, delete denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cloud_saves").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").delete());

  // Owner delete succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("cloud_saves").doc("save_nfs_1").delete());
});

// 4. LibraryEntry subcollection
test("Library: owner pin/playtime update succeeds; mismatched gameId & cross-user fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("library").get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("library").doc("forza_horizon_6").set(
      validLibraryPayload(ALICE_UID, "forza_horizon_6", created)
    )
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("library").doc("forza_horizon_6").update({
      pinned: false,
      playTimeMinutes: 45,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Mismatched doc ID vs gameId fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("library").doc("watch_dogs").set(
      validLibraryPayload(ALICE_UID, "forza_horizon_6", created)
    )
  );

  // Cross-user fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("library").get());
});

// 5. Cross-Platform Multiplayer Lobbies (Query Alignment + Action-Based Updates + Terminal Lock)
test("Lobbies: query alignment, join/ready/leave actions, host controls, terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Alice creates a PUBLIC lobby and a PRIVATE lobby
  await assertSucceeds(
    aliceDb.collection("lobbies").doc("lobby_pub").set(validLobbyPayload(ALICE_UID, "PUBLIC", created))
  );
  await assertSucceeds(
    aliceDb.collection("lobbies").doc("lobby_priv").set(validLobbyPayload(ALICE_UID, "PRIVATE", created))
  );

  // Unfiltered list query MUST fail (rules are not filters; no blanket reads)
  await assertFails(bobDb.collection("lobbies").get());

  // Filtered Kotlin repository queries MUST succeed
  await assertSucceeds(bobDb.collection("lobbies").where("visibility", "==", "PUBLIC").get());
  await assertSucceeds(aliceDb.collection("lobbies").where("memberUids", "array-contains", ALICE_UID).get());

  // Non-member cannot read PRIVATE lobby
  await assertFails(bobDb.collection("lobbies").doc("lobby_priv").get());

  // Bob joins PUBLIC lobby
  await assertSucceeds(
    bobDb.collection("lobbies").doc("lobby_pub").update({
      memberUids: [ALICE_UID, BOB_UID],
      updatedAt: new Date(Date.now() - 3000),
    })
  );

  // Bob cannot hijack title or hostUid
  await assertFails(
    bobDb.collection("lobbies").doc("lobby_pub").update({
      title: "Hacked Lobby",
      updatedAt: new Date(Date.now() - 2500),
    })
  );

  // Bob cannot add Charlie while joining or ready up for Alice
  await assertFails(
    charlieDb.collection("lobbies").doc("lobby_pub").update({
      memberUids: [ALICE_UID, BOB_UID, CHARLIE_UID, "fake_uid"],
      updatedAt: new Date(Date.now() - 2400),
    })
  );

  // Bob toggles ready ON
  await assertSucceeds(
    bobDb.collection("lobbies").doc("lobby_pub").update({
      readyUids: [BOB_UID],
      updatedAt: new Date(Date.now() - 2000),
    })
  );

  // Bob leaves lobby (removes self from memberUids & readyUids)
  await assertSucceeds(
    bobDb.collection("lobbies").doc("lobby_pub").update({
      memberUids: [ALICE_UID],
      readyUids: [],
      updatedAt: new Date(Date.now() - 1500),
    })
  );

  // Alice launches then closes lobby (CLOSED is terminal)
  await assertSucceeds(
    aliceDb.collection("lobbies").doc("lobby_pub").update({
      status: "IN_SESSION",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("lobbies").doc("lobby_pub").update({
      status: "CLOSED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Terminal state lock: no updates allowed after CLOSED
  await assertFails(
    aliceDb.collection("lobbies").doc("lobby_pub").update({
      status: "OPEN",
      updatedAt: new Date(Date.now() - 100),
    })
  );

  // Non-host cannot delete lobby; host can delete
  await assertFails(bobDb.collection("lobbies").doc("lobby_pub").delete());
  await assertSucceeds(aliceDb.collection("lobbies").doc("lobby_pub").delete());
});
