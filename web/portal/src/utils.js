function getUrlPrefix () {
  if (process !== undefined) {
    return process.env.SERVICE_BASE_URL === undefined ? '' : process.env.SERVICE_BASE_URL
  } else if (window !== undefined) {
    let arr = window.location.href.split('//', 2)
    let scheme = arr.length === 1 ? 'http:' : arr[0]
    let arr1 = (arr.length === 1 ? arr[0] : arr[1]).split('/')
    return `${scheme}//${arr1[0]}`
  } else {
    return null
  }
}

function formatDuration (millisec) {
  let milli = millisec - Math.floor(millisec / 1000) * 1000
  let allsec = millisec / 1000
  let day = Math.floor(allsec / 86400)
  let secinday = allsec - day * 86400
  let hour = Math.floor(secinday / 3600)
  let secinhour = secinday - hour * 3600
  let min = Math.floor(secinhour / 60)
  let secinmin = Math.floor(secinhour - min * 3600)
  var s = ''
  if (day > 0) {
    s += day + ' day' + (day > 1 ? 's ' : ' ')
  }
  if (hour > 0) {
    s += hour + ' hour' + (hour > 1 ? 's ' : ' ')
  }
  if (min > 0) {
    s += min + ' minute' + (min > 1 ? 's ' : ' ')
  }
  if (secinmin > 0) {
    s += secinmin + ' second' + (secinmin > 1 ? 's ' : ' ')
  }
  if (milli > 0) {
    s += milli + ' millisecond' + (milli > 1 ? 's ' : ' ')
  }
  console.log(s)
  return s
}

function objectExists (obj) {
  return (obj !== undefined) && (obj !== null)
}

export default getUrlPrefix
export {formatDuration, objectExists}
