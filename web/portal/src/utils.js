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

export default getUrlPrefix
