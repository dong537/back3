import React from 'react';

export function isPlainObject(v) {
  return v != null && typeof v === 'object' && !Array.isArray(v);
}

export function renderValue(v) {
  if (v == null) return '';
  if (typeof v === 'string' || typeof v === 'number' || typeof v === 'boolean') return String(v);
  if (Array.isArray(v)) return v.map(renderValue).filter(Boolean).join('，');
  if (isPlainObject(v)) {
    // Objects are not rendered directly by this function
    return '';
  }
  return String(v);
}

export function KeyValueGrid({ data }) {
  if (!data) return null;

  // string/number is rendered directly
  if (typeof data === 'string' || typeof data === 'number') {
    return <span className="text-gray-200">{String(data)}</span>;
  }

  // boolean is rendered directly
  if (typeof data === 'boolean') {
    return <span className="text-gray-200">{String(data)}</span>;
  }

  // array is rendered as tags
  if (Array.isArray(data)) {
    const items = data.map((x) => renderValue(x)).filter(Boolean);
    if (items.length === 0) return null;
    return (
      <div className="flex flex-wrap gap-2">
        {items.map((t, i) => (
          <span key={i} className="px-2 py-1 rounded bg-white/5 border border-white/10 text-xs text-gray-200">
            {t}
          </span>
        ))}
      </div>
    );
  }

  // object is rendered as a grid
  if (isPlainObject(data)) {
    const entries = Object.entries(data)
      .filter(([, v]) => v != null && v !== '')
      .map(([k, v]) => [k, v]);

    if (entries.length === 0) return null;

    return (
      <div className="grid grid-cols-2 gap-2">
        {entries.map(([k, v]) => (
          <div key={k} className="flex items-start justify-between gap-3 bg-white/5 border border-white/10 rounded-lg px-3 py-2">
            <div className="text-xs text-gray-400 whitespace-nowrap">{k}</div>
            <div className="text-xs text-gray-200 text-right break-words">
              {Array.isArray(v) || isPlainObject(v) ? (
                <pre className="whitespace-pre-wrap break-words text-xs text-gray-200">{JSON.stringify(v, null, 2)}</pre>
              ) : (
                renderValue(v)
              )}
            </div>
          </div>
        ))}
      </div>
    );
  }

  return <span className="text-gray-200">{String(data)}</span>;
}
