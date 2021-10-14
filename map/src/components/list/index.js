import React from 'react'
import Button from '@material-ui/core/Button';
import Grid from '@material-ui/core/Grid';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import Checkbox from '@material-ui/core/Checkbox';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import AddIcon from '@material-ui/icons/Add';
import { makeStyles } from '@material-ui/styles';

const useStyle = makeStyles({
  container:{
    height:'100%',
    // boxShadow:'2px 2px 2px grey',
    borderRadius:'5px'
  },
  list: {
    height: '87%',
    overflow: 'scroll',
    marginBottom:'10px'
  },
  button:{
    width: '100%'
  }
})

export default function CustomList({ title,rows, handleButtonClick, seeMore,checkedStatus,setCheckedStatus }) {
  const classes = useStyle();

  const [checked, setChecked] = React.useState([...checkedStatus.checked])
  const [selectAll, setSelectAll] = React.useState(checkedStatus.selectAll)

  React.useEffect(()=>{
    setCheckedStatus({checked:[...checked],selectAll})
  },[checked,selectAll])
  
  const handleListItemClick = (row) => {
    const currentTarget = checked.indexOf(row.id)
    const newChecked = [...checked]
    if (currentTarget === -1) {
      newChecked.push(row.id)
    } else {
      newChecked.splice(currentTarget, 1)
    }
    setChecked(newChecked)
  }

  const handleSelectAll = () => {
    if (selectAll) {
      setChecked([])
    } else {
      const newChecked = []
      for (let row of rows) {
        newChecked.push(row.id)
      }
      setChecked(newChecked)
    }
    setSelectAll(!selectAll)
  }

  return (
    <div className={classes.container}>
      <Grid container>
        <Grid item xs={6}>
          <FormControlLabel
            control={<Checkbox checked={selectAll} onClick={handleSelectAll} />}
            label={title}
          />
        </Grid>
        <Grid item xs={6}>
          <Button
            variant="contained"
            color="primary"
            onClick={() => handleButtonClick(checked)}
          >
            导入
          </Button>
        </Grid>
      </Grid>
      {/* <div className={classes.list}> */}
      <List dense className={classes.list}>
        {rows.map(row => {
          return (
            <ListItem key={row.id} button onClick={() => handleListItemClick(row)}>
              <ListItemIcon>
                <Checkbox
                  checked={checked.indexOf(row.id) !== -1}
                />
              </ListItemIcon>
              <ListItemText primary={row.id} />
            </ListItem>
          )
        })}
      </List>
      {/* </div> */}
      <Button
        className={classes.button}
        variant="contained"
        color="primary"
        onClick={() => seeMore()}
      >
        <AddIcon fontSize="medium" />
      </Button>
    </div>
  )
}