import React from 'react'
import { Map, Markers, InfoWindow, Polyline } from 'react-amap'
import { CarReq } from '../../requests';
import Box from '@mui/material/Box';
import Slider from '@mui/material/Slider';
import Editor from "@monaco-editor/react";
import moment from 'moment';

export default function CustomMap({ selectVehicleRows, selectGantryRows }) {

  // const [map, setMap] = React.useState(null)
  const [infoWindow, setInfoWindow] = React.useState({ visible: false, position: { longitude: 120, latitude: 30 }, content: 'content', size: { width: 500, height: 150 }, offset: [2, -35] });

  const [directions,setDirections] = React.useState([])
  const [direction,setDirection] = React.useState(null)

  const [historyPositions,setHistoryPositions] = React.useState([])

  const [startAndEndTime,setStartAndEndTime] = React.useState(null)
  const [predictTime,setPredictTime] = React.useState('')
  const [predictPositions,setPredictPositions] = React.useState([])

  const [zoom,setZoom]=React.useState(5)
  const [center,setCenter]=React.useState({longitude:120,latitude:37})

  React.useEffect(()=>{
    if(selectVehicleRows){
      const newDirections=[...directions]
      for(const dir of directions){
        if(selectVehicleRows.findIndex(element=>element.id===dir.id)===-1){
          const currentIndex=newDirections.findIndex(element=>element.id===dir.id)
          newDirections.splice(currentIndex,1)
        }
      }
      setDirections(newDirections)
      for(const sr of selectVehicleRows){
        const len=sr.path.length
        if(len===0||len===1){
          setDirection({
            id:sr.id,
            color:sr.color,
            direction:[],
            amap:{},
            path:sr.path
          })
        }else{
          if(newDirections.findIndex(element=>element.id===sr.id)===-1){
            for(let i=0;i<len-1;i++){
              getDirection({id:sr.id,color:sr.color,path:sr.path},sr.path[i],sr.path[i+1],i)
            }
          }else{
            getDirection({id:sr.id,color:sr.color,path:sr.path},sr.path[len-2],sr.path[len-1],len-2)
          }
        }
      }
    }
  },[selectVehicleRows])

  React.useEffect(()=>{
    if(direction){
      const newDirections=[...directions]
      const currentIndex=newDirections.findIndex(element=>element.id===direction.id)
      if(currentIndex===-1){
        newDirections.push(direction)
      }else{
        newDirections[currentIndex].direction={...newDirections[currentIndex].direction,...direction.direction}
        newDirections[currentIndex].amap={...newDirections[currentIndex].amap,...direction.amap}
      }
      setDirections(newDirections)
    }
  },[direction])


  React.useEffect(()=>{
    const newHistoryPositions=[]
    if(selectVehicleRows){
      for(let sr of selectVehicleRows){
        const maxIndex=sr.path.length-1
        for(let i=0;i<maxIndex;i++){
          const{longitude,latitude,timestamp}=sr.path[i]
          newHistoryPositions.push({id:sr.thingId,position:{longitude,latitude},timestamp,offset:{x:-7,y:-7}})
        }
      }
      
    }
    setHistoryPositions(newHistoryPositions)
  },[selectVehicleRows])

  React.useEffect(()=>{
    setPredictTime('')
    setPredictPositions([])
    setStartAndEndTime(getStartAndEndTime(selectVehicleRows))

    const lastIndex=selectVehicleRows.length-1
    if(lastIndex>=0){
      // console.log(selectVehicleRows[lastIndex])
      const {position}=selectVehicleRows[lastIndex]
      setZoom(8)
      setCenter(position)
    }
  },[selectVehicleRows])

  React.useEffect(()=>{
    const newPredictPositions=[]
    if(predictTime){
      const currentPredictTime=moment(predictTime).unix()
      for(let d of directions){
        if(d.path.length<2) continue

        const currentStartTime=moment(d.path[0].timestamp).unix()
        const currentEndTime=moment(d.path[d.path.length-1].timestamp).unix()
        if(currentPredictTime<currentStartTime||currentPredictTime>currentEndTime) continue

        const newPredictPosition=getPredictPosition(currentPredictTime,d)
        if(newPredictPosition){
          newPredictPositions.push(newPredictPosition)
        }
      }
    }
    setPredictPositions(newPredictPositions)
  },[predictTime])

  React.useEffect(()=>{
    const lastIndex=selectGantryRows.length-1
    if(lastIndex>=0){
      const {position}=selectGantryRows[lastIndex]
      setZoom(8)
      setCenter(position)
    }
  },[selectGantryRows])

  const getPredictPosition = (t,d) => {
    if(Object.size(d.direction)>0){
      const {path}=d
      for(let i=0;i<path.length-1;i++){
        const stime=moment(path[i].timestamp).unix()
        const etime=moment(path[i+1].timestamp).unix()
        if(t>=stime&&t<=etime){
          const v=d.amap[i].distance/(etime-stime)
          const len=Math.floor(v*(t-stime))
          let sPath=0
          let ePath=0
          for(let step of d.amap[i].steps){
            sPath=ePath
            ePath+=parseInt(step.distance)
            if(len>=sPath&&len<=ePath){
              ePath=sPath
              for(let tmc of step.tmcs){
                sPath=ePath
                ePath+=parseInt(tmc.distance)
                if(len>=sPath&&len<=ePath){
                  const pp=tmc.polyline.split(';')[0].split(',')
                  return({position:{longitude:parseFloat(pp[0]),latitude:parseFloat(pp[1])},id:d.id,offset:{x:-7,y:-7}})
                }
              }
              break;
            }
          }
          break;
        }
      }
    }
    return null
}

  const getStartAndEndTime=(timeForselectVehicleRows)=>{
    let res={
      start:moment().unix(),
      end:moment('0000-01-01T00:00:00z').unix()
    }
    for(let timeForSelectRow of timeForselectVehicleRows){
      const {path}=timeForSelectRow
      for(let p of path){
        const {timestamp}=p
        const time=moment(timestamp).unix()
        if(time<res.start) res.start=time
        if(time>res.end) res.end=time
      }
    }
    if(res.start>res.end) return null
    return {
      start:moment.unix(res.start).toISOString(),
      end:moment.unix(res.end).toISOString()
    }
  } 

  const getDirection = (context,origin,destination,index) => {
    CarReq.direction(origin,destination).then(data=>{
      if(data.status==='1'){
        let d=[]
        const path=data.route.paths[0]
        const {steps}=path
        steps.forEach((stepValue,stepIndex)=>{
          const {polyline:stepPolyline}=stepValue
          const stepLngLats=stepPolyline.split(';')
          stepLngLats.forEach((stepLngLatValue)=>{
            const lnglat=stepLngLatValue.split(',')
            d.push({longitude:parseFloat(lnglat[0]),latitude:parseFloat(lnglat[1])})
          })

        })
        setDirection({
          ...context,
          direction:{
            [index]:d
          },
          amap:{
            [index]:path
          }
        })
      }else{
        setDirection({
          ...context,
          direction:{
            [index]:[
              {...origin},
              {...destination}
              ]
          },
          amap:{
            [index]:{}
          }
        })
      }
    })
  }

  return (
    <Map
      amapkey={'c4682e400c06b2b8be5e65b99c6404f5'}
      zoom={zoom}
      center={center}
      events={{
        created: (ins) => {
          // setMap(ins)
        },
        zoomchange: (e) => {
        }
      }}
    >
      <InfoWindow
        position={infoWindow.position}
        // content={infoWindow.content}
        visible={infoWindow.visible}
        size={infoWindow.size}
        isCustom={false}
        offset={infoWindow.offset}
        events={{
          close: (e) => {
            setInfoWindow({ ...infoWindow, visible: false })
          }
        }}
      >
        <Editor
          height='90vh'
          language="json"
          value={infoWindow.content}
        />
      </InfoWindow>

      <Markers
        markers={selectVehicleRows}
        useCluster={true}
        zIndex={10}
        events={{
          click: (e, marker) => {
            const extData = marker.getExtData()
            const { lng: longitude, lat: latitude } = marker.getPosition()
            setInfoWindow({ ...infoWindow, visible: true, position: { longitude, latitude }, content: JSON.stringify(extData.ditto, null, 2), size: { width: 500, height: 500 }, offset: [0, -35] })
          },
          mouseover: (e, marker) => {
            // const extData = marker.getExtData()
            // const { lng: longitude, lat: latitude } = marker.getPosition()
            // setInfoWindow({ ...infoWindow, visible: true, position: { longitude, latitude }, content: JSON.stringify(rows.find(row=>row.id===extData.id)) })
          },
          mouseout: (e, marker) => {
            // const extData = marker.getExtData()
            // setInfoWindow({ ...infoWindow, visible: false })
          },
        }}
        render={extData => {
          return (
            <div
              style={{
                background: `url(https://img.icons8.com/fluency/48/000000/car.png`,
                backgroundSize: 'contain',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                width: '48px',
                height: '48px',
              }}
            ></div>
          )
        }}
      >
      </Markers>

      {directions.length > 0 ? directions.map(dir => {
        let path = []
        for (const i in dir.direction) {
          path = [...path, ...dir.direction[i]]
        }
        return (
          <Polyline
            path={path}
            visible={true}
            style={{ strokeColor: dir.color }}
            zIndex={9}
          >
          </Polyline>
        )
      }) : null}

      <Markers
        markers={historyPositions}
        useCluster={false}
        zIndex={10}
        events={{
          click: (e, marker) => {
            const extData = marker.getExtData()
            const { id, position: { longitude, latitude }, timestamp } = extData
            setInfoWindow({ ...infoWindow, visible: true, position: { longitude, latitude }, content: JSON.stringify({ id, timestamp }, null, 2), size: { width: 500, height: 100 }, offset: [0, -35] })
          }
        }}
        render={extData => {
          return (
            <div
              style={{
                background: `url(https://img.icons8.com/office/30/000000/car.png`,
                backgroundSize: 'contain',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                width: '30px',
                height: '30px',
              }}
            ></div>
          )
        }}
      >
      </Markers>

      {/**预测车辆位置 */}
      <Markers
        markers={predictPositions}
        useCluster={false}
        zIndex={11}
        events={{
          click: (e, marker) => {
            // const extData = marker.getExtData()
            // const {position:{longitude,latitude},id}=extData
            // setInfoWindow({ ...infoWindow, visible: true, position: { longitude, latitude }, content: id,size:{width:180,height:25}, offset:[0,-35]})
          }
        }}
        render={extData => {
          return (
            <div
              style={{
                background: `url(https://img.icons8.com/material-sharp/24/000000/car.png`,
                backgroundSize: 'contain',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                width: '24px',
                height: '24px',
              }}
            ></div>
          )
        }}
      >
      </Markers>

      <Markers
        markers={selectGantryRows}
        useCluster={true}
        zIndex={10}
        events={{
          click: (e, marker) => {
            const extData = marker.getExtData()
            const { position: { longitude, latitude } } = extData
            setInfoWindow({ ...infoWindow, visible: true, position: { longitude, latitude }, content: JSON.stringify(extData.ditto, null, 2), size: { width: 500, height: 250 }, offset: [0, -35] })
          }
        }}
        render={extData => {
          return (
            <div
              style={{
                background: `url(https://img.icons8.com/office/30/000000/overhead-crane.png`,
                backgroundSize: 'contain',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'center',
                width: '30px',
                height: '30px',
              }}
            ></div>
          )
        }}
      >
      </Markers>

      <div className="customLayer" style={{ position: 'absolute', right: '100px', bottom: '30px' ,width: '80%'}}>
        <Box sx={{ width: '100%' }}>
          <Slider 
            min={0} 
            max={1000} 
            valueLabelDisplay='auto' 
            defaultValue={0} 
            aria-label="slider"
            disabled={startAndEndTime?false:true}
            valueLabelFormat={(x) => {
              var start = 0
              var end = 0
              if (startAndEndTime) {
                start = moment(startAndEndTime.start).unix()
                end = moment(startAndEndTime.end).unix()
              }
              const t = (end - start) * x / 1000
              return moment.unix(start + t).toISOString()
            }}
            onChange={(event, value, activeThumb) => {

            }}
            onChangeCommitted={(event, value) => {
              if (startAndEndTime) {
                const start = moment(startAndEndTime.start).unix()
                const end = moment(startAndEndTime.end).unix()
                const t = (end - start) * value / 1000
                setPredictTime(moment.unix(start + t).toISOString())
              }
            }}
          />
        </Box>
      </div>

    </Map>
  )
}