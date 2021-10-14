import React from 'react'
import Grid from '@material-ui/core/Grid';
import { makeStyles } from '@material-ui/styles';
import { CarReq, subcribe } from '../requests';
import { List, Map } from '../components';
import PropTypes from 'prop-types';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import moment from 'moment';

function TabPanel(props) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 ,height:'100%'}}>
          <Typography component={'span'} height={'100%'}>{children}</Typography>
        </Box>
      )}
    </div>
  );
}

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.number.isRequired,
  value: PropTypes.number.isRequired,
};

function a11yProps(index) {
  return {
    id: `simple-tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  };
}


const useStyle = makeStyles({
  container: {
    height: '100%',
  },
  list: {
    height: '92%',
    overflow: 'scroll'
  },
  tabpanel:{
    height:'88%'
  }
})

export default function MapPage() {
  const classes = useStyle();
  const [vehicles, setVehicles] = React.useState([])
  const [vehiclesCallback, setVehiclesCallback] = React.useState(null)
  const [gantries, setGantries] = React.useState([])
  const [gantriesCallback, setGantriesCallback] = React.useState(null)

  const [selectVehicleRow, setSelectVehicleRow] = React.useState(null)
  const [selectVehicleRows, setSelectVehicleRows] = React.useState([])
  const [message, setMessage] = React.useState(null)

  const [selectGantryRows, setSelectGantryRows] = React.useState([])

  const [moreVehicles, setMoreVehicles] = React.useState({ cursor: '', more: false })
  const [moreGantries, setMoreGantries] = React.useState({ cursor: '', more: false })

  const [value, setValue] = React.useState(0);
  
  const [vehicleCheckedStatus,setVehicleCheckedStatus]=React.useState({checked:[],selectAll:false})
  const [gantryCheckedStatus,setGantryCheckedStatus]=React.useState({checked:[],selectAll:false})

  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  Object.size = function (obj) {
    var size = 0,
      key;
    for (key in obj) {
      if (obj.hasOwnProperty(key)) size++;
    }
    return size;
  };

  const getVehicles = (cursor) => {
    CarReq.getVehicles(cursor).then(data => {
      const { items, cursor: newCursor } = data
      const newRows = items.map(item => {
        const { thingId } = item
        return { id: thingId.replace('ics.rodaki:vehicle-', ''), ditto: item }
      })
      setVehiclesCallback(newRows)
      if (newCursor) {
        setMoreVehicles({ cursor: newCursor, more: true })
      } else {
        setMoreVehicles({ cursor: '', more: false })
      }
    })
  }

  const getGantries = (cursor) => {
    CarReq.getGantries(cursor).then(data => {
      let { items, cursor: newCursor } = data
      const newRows = items.map(item => {
        const { thingId, attributes: { longtitude: longitude, latitude } } = item
        return { id: thingId.replace('ics.rodaki:gantry-', ''), position: { longitude, latitude }, ditto: item }
      })
      setGantriesCallback(newRows)
      if (newCursor) {
        setMoreGantries({ cursor: newCursor, more: true })
      } else {
        setMoreGantries({ cursor: '', more: false })
      }
    })
  }

  React.useEffect(() => {
    getVehicles('')
    getGantries('')
  }, [])

  React.useEffect(() => {
    if (vehiclesCallback) {
      setVehicles([...vehicles, ...vehiclesCallback])
    }
  }, [vehiclesCallback])

  React.useEffect(() => {
    if (gantriesCallback) {
      setGantries([...gantries, ...gantriesCallback])
    }
  }, [gantriesCallback])

  React.useEffect(() => {
    if (selectVehicleRow) {
      setSelectVehicleRows([...selectVehicleRows, selectVehicleRow])
    }
  }, [selectVehicleRow])

  React.useEffect(() => {
    if (message) {
      console.log(message)
      const { thingId, features: { location: { properties: { value } } } } = JSON.parse(message)
      let newselectVehicleRows = [...selectVehicleRows]
      const currentIndex = newselectVehicleRows.findIndex(element => element.thingId === thingId)
      const LngLat = value.split(';')
      const position = { longitude: parseFloat(LngLat[0]), latitude: parseFloat(LngLat[1]), timestamp: moment().toISOString() }
      newselectVehicleRows[currentIndex].position = position
      newselectVehicleRows[currentIndex].path = [...newselectVehicleRows[currentIndex].path, position]
      newselectVehicleRows[currentIndex].ditto.features.location.properties.value = value
      setSelectVehicleRows(newselectVehicleRows)
    }
  }, [message])

  const handleVehicleButtonClick = (checked) => {
    const newselectVehicleRows = [...selectVehicleRows]
    for (const selectVehicleRow of selectVehicleRows) {
      if (checked.indexOf(selectVehicleRow.id) === -1) {
        const currentIndex = newselectVehicleRows.findIndex(element => element.id === selectVehicleRow.id)
        selectVehicleRow.sse.close()
        newselectVehicleRows.splice(currentIndex, 1)
      }
    }
    setSelectVehicleRows(newselectVehicleRows)
    if (newselectVehicleRows.length < checked.length) {
      for (const ckd of checked) {
        if (newselectVehicleRows.findIndex(element => element.id === ckd) === -1) {
          CarReq.get(ckd).then(data => {
            const positions = transformFormat(data)
            const currentIndex = positions.length - 1
            const row = vehicles.find(element => element.id === ckd)
            setSelectVehicleRow({
              id: ckd,
              thingId: row.ditto.thingId,
              position: currentIndex >= 0 ? positions[currentIndex] : {},
              path: positions,
              color: 'rgb(' + Math.floor(Math.random() * 255) + ',' + Math.floor(Math.random() * 255) + ',' + Math.floor(Math.random() * 255) + ')',
              sse: subcribe(row.ditto.thingId, (mes) => {
                setMessage(mes)
              }),
              ditto: row.ditto
            })
          })
        }
      }
    }
  }

  const handleGantryButtonClick = (checked) => {
    const newselectGantryRows = [...selectGantryRows]
    for (const selectGantryRow of selectGantryRows) {
      if (checked.indexOf(selectGantryRows.id) === -1) {
        const currentIndex = newselectGantryRows.findIndex(element => element.id === selectGantryRow.id)
        newselectGantryRows.splice(currentIndex, 1)
      }
    }
    if (newselectGantryRows.length < checked.length) {
      for (const ckd of checked) {
        if (newselectGantryRows.findIndex(element => element.id === ckd) === -1) {
          newselectGantryRows.push(gantries.find(g => g.id === ckd))
        }
      }
    }
    setSelectGantryRows(newselectGantryRows)
  }

  const transformFormat = (positions) => {
    const res = []
    for (let p of positions) {
      if (p[1] !== 'null') {
        const LngLat = p[1].split(';')
        res.push({ longitude: parseFloat(LngLat[0]), latitude: parseFloat(LngLat[1]), timestamp: p[0] })
      }
    }
    return res
  }

  const vehicleSeeMore = () => {
    if (moreVehicles.more) {
      getVehicles(moreVehicles.cursor)
    }
  }

  const gantrySeeMore = () => {
    if (moreGantries.more) {
      getGantries(moreGantries.cursor)
    }
  }


  return (
    <Grid container spacing={2} className={classes.container}>
      {/* <Grid item xs={2} className={classes.container}>
        <List title='车辆' rows={vehicles} handleButtonClick={handleVehicleButtonClick} seeMore={vehicleSeeMore} />
      </Grid>
      <Grid item xs={2.2} className={classes.container}>
        <List title='门架' rows={gantries} handleButtonClick={handleGantryButtonClick} seeMore={gantrySeeMore} />
      </Grid>
      <Grid item xs={7.8} className={classes.container}>
        <Map selectVehicleRows={selectVehicleRows} selectGantryRows={selectGantryRows} />
      </Grid> */}
      <Grid item xs={3} className={classes.container}>
        <Box sx={{ width: '100%',height:'100%' }}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider'}}>
            <Tabs value={value} onChange={handleChange} aria-label="basic tabs example" centered >
              <Tab label="车辆" {...a11yProps(0)} />
              <Tab label="门架" {...a11yProps(1)} />
            </Tabs>
          </Box>
          <TabPanel value={value} index={0} className={classes.tabpanel}>
            <List title='全选' rows={vehicles} handleButtonClick={handleVehicleButtonClick} seeMore={vehicleSeeMore} checkedStatus={vehicleCheckedStatus} setCheckedStatus={setVehicleCheckedStatus}/>
          </TabPanel>
          <TabPanel value={value} index={1} className={classes.tabpanel}>
            <List title='全选' rows={gantries} handleButtonClick={handleGantryButtonClick} seeMore={gantrySeeMore} checkedStatus={gantryCheckedStatus} setCheckedStatus={setGantryCheckedStatus}/>
          </TabPanel>
        </Box>
      </Grid>
      <Grid item xs={9} className={classes.container}>
        <Map selectVehicleRows={selectVehicleRows} selectGantryRows={selectGantryRows} />
      </Grid>
    </Grid>

  )
}