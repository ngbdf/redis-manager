/* eslint-disable indent */
export function formatTime (timestamp) {
    let time = new Date(timestamp)
    let y = time.getFullYear()
    let M = time.getMonth() + 1
    let d = time.getDate()
    let h = time.getHours()
    let m = time.getMinutes()
    let s = time.getSeconds()
    return y + '-' + addZero(M) + '-' + addZero(d) + ' ' + addZero(h) + ':' + addZero(m) + ':' + addZero(s)
}

export function formatTimeForChart (timestamp) {
    let time = new Date(timestamp)
    let M = time.getMonth() + 1
    let d = time.getDate()
    let h = time.getHours()
    let m = time.getMinutes()
    return addZero(M) + '/' + addZero(d) + ' ' + addZero(h) + ':' + addZero(m)
}

function addZero (t) {
    return t < 10 ? '0' + t : t
}
