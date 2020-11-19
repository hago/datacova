class JobStorage {
  get (id) {
    let key = `job_${id}`
    let value = sessionStorage.getItem(key)
    return (value === null) || (value.expire < Date.now()) ? null : value.data
  }

  put (id, data, expire = 1800) {
    let key = `job_${id}`
    sessionStorage.setItem(key, JSON.stringify({
      data: data,
      expire: Date.now() + expire * 1000
    }))
  }
}

export default JobStorage
