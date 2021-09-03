package views.checkIns

import com.studo.campusqr.common.CheckIns
import react.*
import webcore.materialUI.*

interface CheckInRowProps : RProps {
  var classes: CheckInRowClasses
  var config: Config

  class Config(
    val checkIn: CheckIns,
  )
}

interface CheckInState : RState

class CheckInRow : RComponent<CheckInRowProps, CheckInState>() {
  override fun RBuilder.render() {
    mTableRow {
      mTableCell {
        +props.config.checkIn.email
      }
      mTableCell {
        +props.config.checkIn.checkInDate
      }
    }
  }
}


interface CheckInRowClasses

private val CheckInRowStyle = { _: dynamic ->
}

private val styled = withStyles<CheckInRowProps, CheckInRow>(CheckInRowStyle)

fun RBuilder.renderCheckInRow(config: CheckInRowProps.Config) = styled {
  attrs.config = config
}
