export default function EmptyState({ title, hint }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      {hint && <p style={{ margin: '0 auto' }}>{hint}</p>}
    </div>
  )
}
