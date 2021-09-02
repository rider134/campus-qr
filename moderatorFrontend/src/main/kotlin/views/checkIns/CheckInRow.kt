package views.checkIns

import com.studo.campusqr.common.ActiveCheckIn
import com.studo.campusqr.common.extensions.format
import kotlinx.browser.window
import react.*
import util.Strings
import util.apiBase
import util.get
import views.accessManagement.AccessManagementDetailsProps
import views.guestCheckIn.locationIdWithSeat
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import kotlin.js.Date

interface CheckInRowProps : RProps {
  var classes: CheckInRowClasses
  var config: Config

  class Config(
    val activeCheckIn: ActiveCheckIn,
  )
}

interface CheckInState : RState

class CheckInRow : RComponent<CheckInRowProps, CheckInState>() {
  override fun RBuilder.render() {
    mTableRow {
      mTableCell {
        +props.config.activeCheckIn.email
      }
      mTableCell {
        +Date(props.config.activeCheckIn.checkInDate).toString()
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
