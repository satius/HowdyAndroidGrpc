package com.github.sugiyamas.howdygrpc

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

interface HowdyWorldView : HowdyWorldEventHandlers {
    val vm: HowdyWorldViewModel
}

class HowdyWorldViewModel : BaseObservable() {
    @get:Bindable
    var hostText: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.hostText)
        }

    @get:Bindable
    var portText: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.portText)
        }

    @get:Bindable
    var messageText: String = "hogehoge"
        set(value) {
            field = value
            notifyPropertyChanged(BR.messageText)
        }

    @get:Bindable
    var resultText: String = "Response:"
}

interface HowdyWorldEventHandlers {
    fun onRequestButtonClick(view: View)
}
