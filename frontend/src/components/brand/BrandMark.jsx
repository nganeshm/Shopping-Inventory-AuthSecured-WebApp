import StorefrontRounded from '@mui/icons-material/StorefrontRounded'

/**
 * E-Marketplace logomark — MUI storefront icon (sidebar, top bar, auth).
 */
export default function BrandMark({ size = 22, className = '' }) {
  return (
    <StorefrontRounded
      className={`brand-mark-svg ${className}`.trim()}
      htmlColor="currentColor"
      sx={{ fontSize: size, display: 'block' }}
      aria-hidden
    />
  )
}
