import { useEffect, useRef, useState } from 'react'
import PersonOutlined from '@mui/icons-material/PersonOutlined'
import BadgeOutlined from '@mui/icons-material/BadgeOutlined'
import EmailOutlined from '@mui/icons-material/EmailOutlined'
import PhoneOutlined from '@mui/icons-material/PhoneOutlined'
import AlternateEmailOutlined from '@mui/icons-material/AlternateEmailOutlined'
import HomeOutlined from '@mui/icons-material/HomeOutlined'
import LocationOnOutlined from '@mui/icons-material/LocationOnOutlined'
import MarkunreadMailboxOutlined from '@mui/icons-material/MarkunreadMailboxOutlined'
import PublicOutlined from '@mui/icons-material/PublicOutlined'
import ApartmentOutlined from '@mui/icons-material/ApartmentOutlined'
import CalendarMonthOutlined from '@mui/icons-material/CalendarMonthOutlined'
import VerifiedOutlined from '@mui/icons-material/VerifiedOutlined'
import TagOutlined from '@mui/icons-material/TagOutlined'
import ContactMailOutlined from '@mui/icons-material/ContactMailOutlined'
import WorkOutlineOutlined from '@mui/icons-material/WorkOutlineOutlined'
import CircularProgress from '@mui/material/CircularProgress'
import { useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { customerApi } from '../api/customerApi'
import { getFriendlyErrorMessage } from '../utils/errors'
import { USER_MESSAGES } from '../constants/userMessages'
import ErrorBanner from '../components/common/ErrorBanner'
import EmptyState from '../components/common/EmptyState'

function formatJoined(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })
}

function initials(firstName, lastName, username) {
  const fromName = `${firstName?.[0] || ''}${lastName?.[0] || ''}`.trim()
  if (fromName) return fromName.toUpperCase()
  return (username || '?').slice(0, 2).toUpperCase()
}

function DetailRow({ icon: Icon, label, value, tone = 'indigo' }) {
  if (!value) return null
  return (
    <li className="profile-detail">
      <span className={`profile-detail-icon profile-detail-icon--${tone}`} aria-hidden="true">
        <Icon sx={{ fontSize: 20 }} />
      </span>
      <span className="profile-detail-copy">
        <span className="profile-detail-label">{label}</span>
        <span className="profile-detail-value">{value}</span>
      </span>
    </li>
  )
}

export default function ProfilePage() {
  const { username, email, isAdmin, linkCustomerId } = useAuth()
  const location = useLocation()
  const [customer, setCustomer] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const linkCustomerIdRef = useRef(linkCustomerId)
  linkCustomerIdRef.current = linkCustomerId

  useEffect(() => {
    let cancelled = false
    if (!email) {
      setLoading(false)
      setError(USER_MESSAGES.profile.missingEmail)
      return undefined
    }

    setLoading(true)
    setError(null)

    customerApi
      .getDetailsByEmail(email, { username })
      .then(({ data }) => {
        if (cancelled) return
        setCustomer(data)
        if (data?.id) linkCustomerIdRef.current(data.id)
      })
      .catch(err => {
        if (cancelled) return
        setCustomer(null)
        setError(getFriendlyErrorMessage(err, USER_MESSAGES.profile.loadCustomer))
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [username, email, location.state?.refreshProfile])

  const displayName = customer
    ? `${customer.firstName || ''} ${customer.lastName || ''}`.trim()
    : username
  const contact = customer?.contactDetail
  const addresses = customer?.addresses || []

  return (
    <div className="profile-page">
      <section className="profile-hero">
        <span className="profile-avatar" aria-hidden="true">
          {initials(customer?.firstName, customer?.lastName, username)}
        </span>
        <div className="profile-hero-copy">
          <p className="profile-kicker">Your profile</p>
          <h1>{displayName || 'Profile'}</h1>
          <p className="profile-hero-meta">
            <span>@{username}</span>
            {isAdmin ? <span className="badge badge-amber">admin</span> : (
              <span className="badge badge-success">customer</span>
            )}
            {customer?.active === false ? (
              <span className="badge badge-neutral">inactive</span>
            ) : customer ? (
              <span className="badge badge-success">active</span>
            ) : null}
          </p>
        </div>
      </section>

      {error ? <ErrorBanner message={error} onDismiss={() => setError(null)} /> : null}

      {loading ? (
        <div className="profile-loading">
          <CircularProgress size={28} sx={{ color: 'var(--accent)' }} />
          <span>Loading profile…</span>
        </div>
      ) : null}

      {!loading && customer ? (
        <>
          <div className="profile-grid">
            <article className="profile-card profile-card--account">
              <header className="profile-card-head">
                <span className="profile-card-glyph profile-card-glyph--indigo">
                  <PersonOutlined sx={{ fontSize: 22 }} />
                </span>
                <h2>Account</h2>
              </header>
              <ul className="profile-details">
                <DetailRow icon={BadgeOutlined} tone="indigo" label="Username" value={username} />
                <DetailRow icon={TagOutlined} tone="violet" label="Customer ID" value={customer.id} />
                <DetailRow
                  icon={VerifiedOutlined}
                  tone="gold"
                  label="Role"
                  value={isAdmin ? 'Admin' : 'Customer'}
                />
                <DetailRow
                  icon={CalendarMonthOutlined}
                  tone="coral"
                  label="Member since"
                  value={formatJoined(customer.registeredAt)}
                />
                <DetailRow
                  icon={VerifiedOutlined}
                  tone="mint"
                  label="Status"
                  value={customer.active === false ? 'Inactive' : 'Active'}
                />
              </ul>
            </article>

            <article className="profile-card profile-card--contact">
              <header className="profile-card-head">
                <span className="profile-card-glyph profile-card-glyph--teal">
                  <ContactMailOutlined sx={{ fontSize: 22 }} />
                </span>
                <h2>Personal & contact</h2>
              </header>
              <ul className="profile-details">
                <DetailRow icon={PersonOutlined} tone="indigo" label="First name" value={customer.firstName} />
                <DetailRow icon={PersonOutlined} tone="violet" label="Last name" value={customer.lastName} />
                <DetailRow icon={EmailOutlined} tone="sky" label="Email" value={customer.email} />
                <DetailRow icon={PhoneOutlined} tone="mint" label="Primary phone" value={contact?.primaryPhone} />
                <DetailRow icon={PhoneOutlined} tone="coral" label="Secondary phone" value={contact?.secondaryPhone} />
                <DetailRow icon={AlternateEmailOutlined} tone="gold" label="Alternate email" value={contact?.alternateEmail} />
                <DetailRow
                  icon={ContactMailOutlined}
                  tone="teal"
                  label="Preferred contact"
                  value={contact?.preferredContactMethod}
                />
              </ul>
            </article>
          </div>

          <article className="profile-card profile-card--wide profile-card--address">
            <header className="profile-card-head">
              <span className="profile-card-glyph profile-card-glyph--coral">
                <HomeOutlined sx={{ fontSize: 22 }} />
              </span>
              <h2>Addresses</h2>
              <span className="profile-count">{addresses.length}</span>
            </header>

            {addresses.length === 0 ? (
              <p className="profile-empty">No addresses are saved on this profile yet.</p>
            ) : (
              <div className="profile-address-grid">
                {addresses.map(address => (
                  <div
                    key={address.id || `${address.line1}-${address.postalCode}`}
                    className={`profile-address${address.defaultAddress ? ' profile-address--home' : ' profile-address--work'}`}
                  >
                    <div className="profile-address-top">
                      <span className="profile-address-type">
                        {address.addressType === 'WORK' ? (
                          <WorkOutlineOutlined sx={{ fontSize: 18 }} />
                        ) : (
                          <ApartmentOutlined sx={{ fontSize: 18 }} />
                        )}
                        {address.addressType || 'Address'}
                      </span>
                      {address.defaultAddress ? <span className="badge badge-success">Default</span> : null}
                    </div>
                    <ul className="profile-details">
                      <DetailRow
                        icon={HomeOutlined}
                        tone="coral"
                        label="Street"
                        value={[address.line1, address.line2].filter(Boolean).join(', ')}
                      />
                      <DetailRow
                        icon={LocationOnOutlined}
                        tone="gold"
                        label="City / state"
                        value={[address.city, address.state].filter(Boolean).join(', ')}
                      />
                      <DetailRow icon={MarkunreadMailboxOutlined} tone="sky" label="Postal code" value={address.postalCode} />
                      <DetailRow icon={PublicOutlined} tone="mint" label="Country" value={address.country} />
                    </ul>
                  </div>
                ))}
              </div>
            )}
          </article>
        </>
      ) : null}

      {!loading && !customer && !error ? (
        <EmptyState
          title="No profile found"
          hint="We could not find a customer profile for this email."
        />
      ) : null}
    </div>
  )
}
