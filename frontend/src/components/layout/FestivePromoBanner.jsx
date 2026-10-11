import { useId } from 'react'

/** Flat clay diya — original icon in the common Diwali sticker style. */
function FlatDiya({ size = 36, delay = 0 }) {
  const uid = useId().replace(/:/g, '')
  const bowl = `diyaBowl-${uid}`
  const flame = `diyaFlame-${uid}`
  const glow = `diyaGlow-${uid}`

  return (
    <svg
      className="festive-flat-diya"
      width={size}
      height={size}
      viewBox="0 0 64 64"
      aria-hidden="true"
      style={{ animationDelay: `${delay}s` }}
    >
      <defs>
        <radialGradient id={glow} cx="50%" cy="38%" r="45%">
          <stop offset="0%" stopColor="#fff59d" />
          <stop offset="55%" stopColor="#ffb300" stopOpacity="0.55" />
          <stop offset="100%" stopColor="#ff6f00" stopOpacity="0" />
        </radialGradient>
        <linearGradient id={flame} x1="50%" y1="0%" x2="50%" y2="100%">
          <stop offset="0%" stopColor="#fffde7" />
          <stop offset="35%" stopColor="#ffee58" />
          <stop offset="70%" stopColor="#ff9800" />
          <stop offset="100%" stopColor="#e65100" />
        </linearGradient>
        <linearGradient id={bowl} x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" stopColor="#ffcc80" />
          <stop offset="40%" stopColor="#ef6c00" />
          <stop offset="100%" stopColor="#bf360c" />
        </linearGradient>
      </defs>
      <circle cx="32" cy="28" r="16" fill={`url(#${glow})`} />
      <path
        className="festive-flat-diya-flame"
        d="M32 8c6 10 10 14 10 20a10 10 0 0 1-20 0c0-6 4-10 10-20z"
        fill={`url(#${flame})`}
      />
      <ellipse cx="32" cy="26" rx="3.2" ry="6" fill="#fffde7" opacity="0.9" />
      <path d="M12 42c2-6 10-10 20-10s18 4 20 10c1 4-3 8-20 8S11 46 12 42z" fill={`url(#${bowl})`} />
      <ellipse cx="32" cy="42" rx="19" ry="4.5" fill="#ffe0b2" />
      <path d="M18 41.5c4-2.4 9-3.4 14-3.4s10 1 14 3.4" fill="none" stroke="#fff3e0" strokeWidth="1.2" opacity="0.55" />
      <rect x="30.4" y="36" width="3.2" height="6" rx="1.4" fill="#5d4037" />
    </svg>
  )
}

/** Flat marigold — original icon in the common Diwali sticker style. */
function FlatFlower({ size = 28, delay = 0 }) {
  const uid = useId().replace(/:/g, '')
  const petal = `flowerPetal-${uid}`
  const center = `flowerCenter-${uid}`

  return (
    <svg
      className="festive-flat-flower"
      width={size}
      height={size}
      viewBox="0 0 64 64"
      aria-hidden="true"
      style={{ animationDelay: `${delay}s` }}
    >
      <defs>
        <linearGradient id={petal} x1="50%" y1="0%" x2="50%" y2="100%">
          <stop offset="0%" stopColor="#ffe082" />
          <stop offset="55%" stopColor="#ff9800" />
          <stop offset="100%" stopColor="#ef6c00" />
        </linearGradient>
        <radialGradient id={center} cx="50%" cy="45%" r="55%">
          <stop offset="0%" stopColor="#fff59d" />
          <stop offset="70%" stopColor="#f57c00" />
          <stop offset="100%" stopColor="#e65100" />
        </radialGradient>
      </defs>
      {[0, 45, 90, 135, 180, 225, 270, 315].map(deg => (
        <ellipse
          key={deg}
          cx="32"
          cy="16"
          rx="8"
          ry="14"
          fill={`url(#${petal})`}
          transform={`rotate(${deg} 32 32)`}
        />
      ))}
      <circle cx="32" cy="32" r="10" fill={`url(#${center})`} />
      <circle cx="32" cy="32" r="4" fill="#bf360c" />
    </svg>
  )
}

/** Flat mango / sacred leaf — original icon in the common Diwali sticker style. */
function FlatLeaf({ size = 26, flip = false, delay = 0 }) {
  const uid = useId().replace(/:/g, '')
  const fill = `leafFill-${uid}`

  return (
    <svg
      className={`festive-flat-leaf${flip ? ' festive-flat-leaf--flip' : ''}`}
      width={size}
      height={Math.round(size * 1.35)}
      viewBox="0 0 40 56"
      aria-hidden="true"
      style={{ animationDelay: `${delay}s` }}
    >
      <defs>
        <linearGradient id={fill} x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#aed581" />
          <stop offset="50%" stopColor="#43a047" />
          <stop offset="100%" stopColor="#1b5e20" />
        </linearGradient>
      </defs>
      <path
        d="M20 3C10 12 5 26 8 40c2 8 7 12 12 13 5-1 10-5 12-13 3-14-2-28-12-37z"
        fill={`url(#${fill})`}
      />
      <path d="M20 10v36" stroke="#e8f5e9" strokeWidth="1.6" strokeLinecap="round" opacity="0.7" />
      <path d="M20 18c5 3 8 7 9 12" fill="none" stroke="#c8e6c9" strokeWidth="1.1" opacity="0.8" />
      <path d="M20 24c-5 3-8 7-9 12" fill="none" stroke="#c8e6c9" strokeWidth="1.1" opacity="0.8" />
    </svg>
  )
}

function FlatSpark({ size = 16, delay = 0 }) {
  return (
    <svg
      className="festive-flat-spark"
      width={size}
      height={size}
      viewBox="0 0 24 24"
      aria-hidden="true"
      style={{ animationDelay: `${delay}s` }}
    >
      <path
        fill="#ffb300"
        d="M12 2l1.6 6.2L20 10l-6.4 1.8L12 18l-1.6-6.2L4 10l6.4-1.8L12 2z"
      />
    </svg>
  )
}

function PromoSegment() {
  return (
    <span className="festive-promo-segment">
      <span className="festive-promo-chip festive-promo-chip--festival">Diwali & Dussehra</span>
      <span className="festive-promo-text">Festive shopping — get</span>
      <span className="festive-promo-chip festive-promo-chip--discount">10% OFF</span>
      <span className="festive-promo-text">on cart total of</span>
      <span className="festive-promo-chip festive-promo-chip--amount">₹5,000+</span>
      <span className="festive-promo-spark" aria-hidden="true">
        ✦
      </span>
      <span className="festive-promo-text">Celebrate with tradition & savings</span>
      <span className="festive-promo-divider" aria-hidden="true">
        •
      </span>
    </span>
  )
}

function BannerDecor({ side }) {
  const flip = side === 'right'

  return (
    <div className={`festive-promo-diyas festive-promo-diyas--${side}`} aria-hidden="true">
      <FlatLeaf size={22} flip={flip} delay={0.1} />
      <FlatFlower size={26} delay={0.2} />
      <FlatDiya size={side === 'right' ? 38 : 34} delay={0} />
      <FlatFlower size={20} delay={0.45} />
      <FlatLeaf size={18} flip={!flip} delay={0.3} />
      <FlatSpark size={12} delay={0.55} />
    </div>
  )
}

export default function FestivePromoBanner() {
  return (
    <div className="festive-promo-shell">
      <div className="festive-promo-border-glow" aria-hidden="true" />
      <div className="festive-promo" role="region" aria-label="Festive shopping offer">
        <div className="festive-promo-shimmer" aria-hidden="true" />
        <div className="festive-promo-lights" aria-hidden="true">
          {Array.from({ length: 10 }).map((_, i) => (
            <span key={i} className="festive-light" style={{ animationDelay: `${i * 0.35}s` }} />
          ))}
        </div>

        <BannerDecor side="left" />

        <div className="festive-promo-viewport">
          <div className="festive-promo-track">
            <div className="festive-promo-line">
              <PromoSegment />
              <PromoSegment />
              <PromoSegment />
            </div>
            <div className="festive-promo-line" aria-hidden="true">
              <PromoSegment />
              <PromoSegment />
              <PromoSegment />
            </div>
          </div>
        </div>

        <BannerDecor side="right" />
      </div>
    </div>
  )
}
