import type Identity from "@/entities/identity";

export function addTokenHeader(user: Identity, headers?: Headers): Headers {
    let h = headers === undefined ? new Headers() : headers
    h.append("authorization", `token ${user.token}`)
    return h
}

export default {}
