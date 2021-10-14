import axios from "axios";

const backendApi = 'http://localhost:10086/apis/ListHistoricalLocation'
const dittoApi='http://localhost:8080/'

export async function get(id) {
  let body = new FormData();
  body.append('id', id)
  body.append('startTime', '2020-06-16T13:02:00')
  const { data } = await axios.request({
    url: backendApi,
    data: body,
    method: 'POST',
  })
  return data
}
export async function getVehicles(cursor) {
  var url = dittoApi+'api/2/search/things?filter=eq(definition,"ics.rodaki:vehicle:1.0")&option=size(200)'
  if (cursor) {
    url += ',cursor(' + cursor + ')'
  }
  const { data } = await axios.request({
    url,
    method: 'GET',
    auth: {
      username: 'ditto',
      password: 'ditto'
    }
  })
  return data
}
export async function getGantries(cursor) {
  var url =  dittoApi+'api/2/search/things?filter=eq(definition,"ics.rodaki:gantry:1.0")&option=size(200)'
  if (cursor) {
    url += ',cursor(' + cursor + ')'
  }
  const { data } = await axios.request({
    url,
    method: 'GET',
    auth: {
      username: 'ditto',
      password: 'ditto'
    }
  })
  return data
}
export async function direction(origin, destination) {
  const url = `https://restapi.amap.com/v3/direction/driving?key=f4833b485afbe530c057be70b1893ed5&destination=` + destination.longitude + `,` + destination.latitude + `&origin=` + origin.longitude + `,` + origin.latitude
  const { data } = await axios.request({
    url,
    method: 'GET',
  })
  return data
}
