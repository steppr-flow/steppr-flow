⏺ Imagine ce scénario avec ton workflow de commande:

Step 1: ✅ validateOrder      → OK
Step 2: ✅ reserveInventory   → OK (stock réservé)
Step 3: ❌ processPayment     → ÉCHEC (carte refusée)

Problème actuel: Le stock reste réservé alors que la commande a échoué.

Saga = Compensation automatique

Chaque step définit son action et son rollback:

@Step(id = 2, label = "Reserve Inventory")
public void reserveInventory(OrderPayload payload) {
inventoryService.reserve(payload.getItems());
payload.setReservationId("RES-123");
}

@Compensate(step = 2)  // ← Rollback si un step suivant échoue
public void releaseInventory(OrderPayload payload) {
inventoryService.release(payload.getReservationId());
}

Avec Saga, quand Step 3 échoue:

Step 1: ✅ validateOrder
Step 2: ✅ reserveInventory   → stock réservé
Step 3: ❌ processPayment     → ÉCHEC

          ↓ Compensations déclenchées automatiquement ↓

Compensate 2: 🔄 releaseInventory  → stock libéré
Compensate 1: 🔄 (rien à faire)

Exemple concret

Sans Saga:
┌─────────┐    ┌─────────┐    ┌─────────┐
│ Reserve │ →  │ Payment │ →  │  FAIL   │
│  Stock  │    │  FAIL   │    │ Stock   │
│   ✅    │    │   ❌    │    │ bloqué! │
└─────────┘    └─────────┘    └─────────┘

Avec Saga:
┌─────────┐    ┌─────────┐    ┌─────────┐
│ Reserve │ →  │ Payment │ →  │ Release │
│  Stock  │    │  FAIL   │    │  Stock  │
│   ✅    │    │   ❌    │    │   🔄    │
└─────────┘    └─────────┘    └─────────┘

Actuellement dans Steppr Flow

Tu gères ça manuellement dans @OnFailure:

@OnFailure
public void onFailed(OrderPayload payload, Throwable error) {
// Tu dois gérer toi-même le rollback
if (payload.getReservationId() != null) {
inventoryService.release(payload.getReservationId());
}
}

Avec le pattern Saga intégré, ce serait automatique et déclaratif.
