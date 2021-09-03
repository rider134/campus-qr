package views.checkIns

import com.studo.campusqr.common.CheckIns
import com.studo.campusqr.common.ClientLocation
import react.*
import util.Strings
import util.apiBase
import util.get
import views.common.*
import webcore.MbSnackbarProps
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbSnackbar
import kotlinext.js.js

interface CheckInOverviewProps : RProps {
  var classes: CheckInOverviewClasses
}

interface CheckInOverviewState : RState {
  var activeCheckIns: List<CheckIns>?
  var showAddGuestCheckInDialog: Boolean
  var snackbarText: String

  var fetchInProgress: Boolean
  var locationNameToLocationMap: Map<String, ClientLocation>

  var selectedLocation: ClientLocation?
  var selectedLocationTextFieldError: String
}

class CheckInOverview : RComponent<CheckInOverviewProps, CheckInOverviewState>() {

  override fun CheckInOverviewState.init() {
    activeCheckIns = emptyList()
    showAddGuestCheckInDialog = false
    snackbarText = ""

    fetchInProgress = false
    locationNameToLocationMap = emptyMap()

    selectedLocation = null
    selectedLocationTextFieldError = ""
  }

  private fun fetchLocations() = launch {
    setState {
      fetchInProgress = true
    }
    val response = NetworkManager.get<Array<ClientLocation>>("$apiBase/location/list")
    setState {
      if (response != null) {
        locationNameToLocationMap = response.associateBy { it.name }
      }
      fetchInProgress = false
    }
  }

  private fun fetchActiveCheckIns() = launch {
    setState { fetchInProgress = true }
    val response = NetworkManager.get<Array<CheckIns>>("$apiBase/checkIns",)
    setState {
      if (response != null) {
        activeCheckIns = response.toList()
      } else {
        snackbarText = Strings.error_try_again.get()
      }
      fetchInProgress = false
    }
  }

  private fun fetchActiveCheckInsByLocation(locationid: String) = launch {
    setState {  fetchInProgress = true }
    val response = NetworkManager.get<Array<CheckIns>>("$apiBase/checkIns", mapOf("locationid" to locationid))
    setState {
      if (response != null) {
        activeCheckIns = response.toList()
      } else {
        snackbarText = Strings.error_try_again.get()
      }
      fetchInProgress = false
    }
  }

  override fun componentDidMount() {
    fetchLocations()
    fetchActiveCheckIns()
  }

  override fun componentDidUpdate(prevProps: CheckInOverviewProps, prevState: CheckInOverviewState, snapshot: Any) {
    if(prevState.selectedLocation != state.selectedLocation)
    {
        state.selectedLocation?.id?.let {
          fetchActiveCheckInsByLocation(it)
        } ?: run{
          fetchActiveCheckIns()
        }
    }
  }


  private fun RBuilder.renderSnackbar() = mbSnackbar(
    MbSnackbarProps.Config(
      show = state.snackbarText.isNotEmpty(),
      message = state.snackbarText,
      onClose = {
        setState { snackbarText = "" }
      })
  )

  override fun RBuilder.render() {
    renderSnackbar()

    typography {
      attrs.className = props.classes.header
      attrs.variant = "h5"
      +Strings.checkin.get()
    }

    muiAutocomplete {
      attrs.className = props.classes.select
      attrs.value = state.selectedLocation?.name ?: ""
      attrs.onChange = { _, target: String?, _ ->
        setState {
          selectedLocationTextFieldError = ""
          selectedLocation = target?.let { locationNameToLocationMap[it] }
        }
      }
      attrs.openOnFocus = true
      attrs.options = state.locationNameToLocationMap.keys.toTypedArray()
      attrs.getOptionLabel = { it }
      attrs.renderInput = { params: dynamic ->
        textField {
          attrs.error = state.selectedLocationTextFieldError.isNotEmpty()
          attrs.helperText = state.selectedLocationTextFieldError
          attrs.id = params.id
          attrs.InputProps = params.InputProps
          attrs.inputProps = params.inputProps
          attrs.disabled = params.disabled
          attrs.fullWidth = params.fullWidth
          attrs.fullWidth = true
          attrs.variant = "outlined"
          attrs.label = Strings.location_name.get()
        }
      }
    }


    renderLinearProgress(state.fetchInProgress)

    when {
      state.activeCheckIns?.isNotEmpty() == true -> mTable {
        mTableHead {
          mTableRow {
            mTableCell { +Strings.email_address.get() }
            mTableCell { +Strings.checkInDate.get() }
            mTableCell { }
          }
        }
        mTableBody {
          state.activeCheckIns!!.forEach { checkIns ->
            renderCheckInRow(
              CheckInRowProps.Config(checkIns)
            )
          }
        }
      }
      state.activeCheckIns == null && !state.fetchInProgress -> networkErrorView()
      !state.fetchInProgress -> genericErrorView(
        Strings.checkin_not_yet_added_title.get(),
        ""
      )
    }
  }
}

interface CheckInOverviewClasses{
  var header: String
  var select: String
}

private val style = { _: dynamic ->
  js {
    header = js {
      margin = 16
    }
    select = js {
      margin = 8
    }
  }
}

private val styled = withStyles<CheckInOverviewProps, CheckInOverview>(style)

fun RBuilder.renderCheckInOverview() = styled {
  // Set component attrs here
}
