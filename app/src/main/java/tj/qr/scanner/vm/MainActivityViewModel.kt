package tj.qr.scanner.vm

import android.graphics.RectF
import androidx.lifecycle.*
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import tj.qr.scanner.mapper.Mapper
import tj.qr.scanner.mapper.ResultPointsMapper
import tj.qr.scanner.model.QRCodeCoordinates
import tj.qr.scanner.model.TouchCoordinate

class MainActivityViewModel : ViewModel() {

    private var mainScope = MainScope()

    private val _resultPoints = MutableLiveData<ArrayList<RectF>>()
    val resultPoints: LiveData<ArrayList<RectF>> = _resultPoints

    private val _touchCoordinate = MutableSharedFlow<TouchCoordinate>()
    val touchCoordinate: LiveData<TouchCoordinate> = _touchCoordinate.asLiveData()

    private val _resultText = MutableSharedFlow<ArrayList<String>>()
    val resultText: LiveData<List<String>> = _resultText.asLiveData()

    private val mapper: Mapper<Array<ResultPoint>, RectF> by lazy {
        ResultPointsMapper()
    }

    fun mapResult(vararg results: Result) {
        viewModelScope.launch {
            val texs = ArrayList<String>()
            val points = ArrayList<RectF>(results.size)
            results.forEach { resultPoint ->
                texs.add(resultPoint.text)
                val model = mapper.map(resultPoint.resultPoints)
                points.add(model)
            }
            _resultText.emit(texs)
            _resultPoints.postValue(points)
        }
    }

    fun startAutoFocus(x: Float, y: Float) {
        viewModelScope.launch {

            if (mainScope.isActive) {
                mainScope.cancel()
                mainScope = MainScope()
            }
            mainScope.launch {
                delay(300)
                _touchCoordinate.emit(TouchCoordinate(x, y))
            }
        }
    }

}