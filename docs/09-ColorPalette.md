# Color Palette & Design Tokens

Material-Design-inspired, dual-theme (Light/Dark), WCAG-AA contrast targeted.

## Brand / Primary
| Token | Light | Dark | Usage |
|---|---|---|---|
| `--color-primary` | `#3B5BFB` | `#7C93FF` | Primary actions, active nav item, links |
| `--color-primary-variant` | `#2A3EB1` | `#4C63D6` | Gradients, pressed states |
| `--color-secondary` | `#00BFA6` | `#37E4C8` | Success accents, "Available" status |
| `--color-accent` | `#FF6B6B` | `#FF8A8A` | Alerts, overdue/destructive actions |

## Surfaces
| Token | Light | Dark |
|---|---|---|
| `--color-bg` | `#F5F7FB` | `#0F1420` |
| `--color-surface` | `#FFFFFF` | `#1A2032` |
| `--color-surface-alt` | `#EEF1F8` | `#232A3F` |
| `--color-border` | `#E1E5F0` | `#2C3448` |

## Typography
| Token | Light | Dark |
|---|---|---|
| `--color-text-primary` | `#1B1F2A` | `#EAF0FF` |
| `--color-text-secondary` | `#5B6478` | `#9AA5C0` |
| `--color-text-disabled` | `#A6ADBE` | `#5C6478` |

Font family: **Inter** (UI text), **JetBrains Mono** (ISBN/barcode/monospace fields).
Type scale: 12 / 14 / 16 / 20 / 24 / 32 px, weight 400/500/600/700.

## Status Colors
| Status | Color | Hex |
|---|---|---|
| Available | Green | `#22C55E` |
| Issued | Blue | `#3B82F6` |
| Overdue | Red | `#EF4444` |
| Reserved | Amber | `#F59E0B` |
| Maintenance / Damaged | Gray-Orange | `#B45309` |
| Lost | Dark Red | `#991B1B` |

## Gradients
- Login background / hero cards: `linear-gradient(135deg, #3B5BFB 0%, #7C3AED 100%)`
- Stat card accents: `linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%)`

## Elevation (Shadows)
| Level | Light | Dark |
|---|---|---|
| 1 (card) | `0 1px 3px rgba(16,24,40,.08)` | `0 1px 3px rgba(0,0,0,.4)` |
| 2 (hover/dropdown) | `0 4px 12px rgba(16,24,40,.12)` | `0 4px 16px rgba(0,0,0,.55)` |
| 3 (modal) | `0 12px 32px rgba(16,24,40,.18)` | `0 12px 32px rgba(0,0,0,.7)` |

## Radii & Spacing
- Corner radius: 8px (inputs/buttons), 12px (cards), 20px (modals/dialogs)
- Spacing scale: 4 / 8 / 12 / 16 / 24 / 32 / 48 px

All tokens are defined once in `base.css` as CSS custom-property-like JavaFX `-fx-*` variables
duplicated per theme in `theme-light.css` / `theme-dark.css`, toggled by swapping the stylesheet on
the root `Scene` (no restart required).
