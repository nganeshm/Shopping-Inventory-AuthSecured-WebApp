export default function LoadingState({ label = 'Loading…' }) {
  return (
    <div className="inline-row" style={{ color: 'var(--ink-muted)', padding: '20px 0' }}>
      <span className="spinner" />
      <span>{label}</span>
    </div>
  )
}
