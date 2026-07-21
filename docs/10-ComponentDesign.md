# Component Design (Reusable UI Component Library)

Housed in `ui/component/`, each is a custom JavaFX `Control`/composite `Region` with its own FXML +
CSS, exposed via JavaFX properties for data binding — no controller ever manipulates raw nodes of
these components directly.

## 1. `StatCard`
- Props: `title`, `value` (bindable `StringProperty`), `icon`, `trend` (+/-%), `accentGradient`
- Behavior: subtle scale/shadow animation on hover; click-through to the detail screen (e.g. "Overdue" → Overdue report)

## 2. `DataTableView<T>`
- Wraps `TableView<T>` with: built-in pagination footer, column sort indicators, row striping,
  empty-state placeholder, loading skeleton state, row-hover elevation, selection checkbox column
  for bulk actions.
- Exposes `ObservableList<T> items`, `IntegerProperty pageSize`, `Predicate<T> filter`.

## 3. `FilterBar`
- Composable row of search field + dropdown filters + "Advanced Filters" popover trigger; emits a
  single `SearchCriteria` object via callback/binding, debounced for real-time search (300ms).

## 4. `SidebarNav`
- Collapsible drawer; each `NavItem` (icon + label + badge) bound to `RoleGuard` visibility;
  active-route highlighting driven by the `ViewNavigator`'s current route property.

## 5. `TopAppBar`
- Global search (opens `GlobalSearchOverlay`), notification bell (badge count bound to unread
  `NotificationService` stream), theme toggle, profile menu (Profile / Change Password / Logout).

## 6. `ConfirmDialog` / `SnackbarHost`
- `ConfirmDialog`: modal, used for all destructive actions (delete, waive fine, cancel PO) — always
  requires explicit confirm, supports a "type to confirm" variant for high-risk deletes.
- `SnackbarHost`: single app-level overlay queue for transient success/error/info toasts with
  optional "Undo" action button (wired to service-layer soft-delete reversal).

## 7. `FormSection` / `ValidatedTextField`
- `ValidatedTextField` wraps `TextField` + inline error label, bound to a `Validator` result;
  red border + shake animation on invalid submit attempt.
- `FormSection`: titled card grouping related fields (e.g. "Book Identifiers", "Physical Location").

## 8. `BarcodeScannerField`
- Text field that also listens for HID scanner "fast keystroke + Enter" pattern and optionally
  activates a camera-based scan popover (ZXing) via a small camera icon button.

## 9. `ChartCard`
- Thin wrapper around JavaFX `LineChart`/`BarChart`/`PieChart` with consistent card chrome, legend
  styling, and empty/loading states; data supplied via `ObservableList<XYChart.Series<>>`.

## 10. `StatusChip`
- Colored pill label for entity status (Available/Issued/Overdue/etc.), color resolved centrally
  from the status-color map in `09-ColorPalette.md` — never hardcoded per screen.

## 11. `LoadingOverlay`
- Full-panel translucent overlay with spinner, shown while an `AsyncExecutor`-wrapped `Task` is
  running; auto-dismisses on completion/failure and routes failures to `GlobalExceptionHandler`
  for a friendly Snackbar message.

## 12. `WizardStepper`
- Used for multi-step flows (Bulk Import review, Purchase Order approval) — step indicator + next/
  back navigation + per-step validation gate.

## Composition Principle
Screens are composed by assembling these primitives inside FXML; screen-specific logic lives only
in the Controller/ViewModel, never inside the reusable components themselves (keeps components
presentation-only and framework for consistency across all 30 modules).
