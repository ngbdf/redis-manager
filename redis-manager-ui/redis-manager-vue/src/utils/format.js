export function formatBytes (bytes) {
  if (bytes === 0) return '0 B'
  let k = 1024 // or 1000
  let sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  let i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(1)} ${sizes[i]}`
}

export function formatterInput (value) {
  return value.toString().replace(/(\d)(?=(\d{3})+(?:\.\d+)?$)/g, '$1,')
};
