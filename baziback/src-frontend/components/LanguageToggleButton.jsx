import { Languages } from 'lucide-react'
import useLanguageToggle from '../hooks/useLanguageToggle'

export default function LanguageToggleButton({
  className = '',
  iconSize = 18,
  title,
  onAfterToggle,
  children,
}) {
  const { label, isPending, toggleLanguage } = useLanguageToggle()

  const handleClick = async () => {
    await toggleLanguage()
    if (typeof onAfterToggle === 'function') {
      onAfterToggle()
    }
  }

  return (
    <button
      type="button"
      onClick={() => {
        void handleClick()
      }}
      disabled={isPending}
      className={className}
      title={title || label}
      aria-label={title || label}
    >
      <Languages size={iconSize} />
      <span>{children || label}</span>
    </button>
  )
}
