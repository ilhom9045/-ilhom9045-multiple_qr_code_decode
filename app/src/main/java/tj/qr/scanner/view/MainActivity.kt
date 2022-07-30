package tj.qr.scanner.view


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.*
import android.view.ScaleGestureDetector
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import tj.qr.scanner.analyze.BarCodeAndQRCodeAnalyser
import tj.qr.scanner.analyze.DecodeQrCode
import tj.qr.scanner.analyze.DecodeQrCodeImpl
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import tj.qr.scanner.R
import tj.qr.scanner.callback.QRCodeFoundListener
import tj.qr.scanner.customView.ScannerOverlayImpl
import tj.qr.scanner.vm.MainActivityViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.properties.Delegates.notNull

class MainActivity : AppCompatActivity(R.layout.activity_main), QRCodeFoundListener {

    private var viewFinder: PreviewView by notNull()
    private var camera: Camera by notNull()
    private var olActScanner: ScannerOverlayImpl by notNull()
    private var resultRecyclerView: RecyclerView by notNull()
    private var clearText: TextView by notNull()
    private var adapter: BarcodeRecyclerViewAdapter by notNull()

    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this)[MainActivityViewModel::class.java]
    }

    private val executor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val imageAnalyzer: ImageAnalysis by lazy {
        ImageAnalysis.Builder()
            .setTargetResolution(Size(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT)).build().also {
                val decoder: DecodeQrCode = DecodeQrCodeImpl(this)
                val analyser = BarCodeAndQRCodeAnalyser(olActScanner, decoder)
                it.setAnalyzer(
                    executor, analyser
                )
            }
    }

    private val preview: Preview =
        Preview.Builder().setTargetResolution(Size(TARGET_PREVIEW_WIDTH, TARGET_PREVIEW_HEIGHT))
            .build()
    private var cameraProvider: ProcessCameraProvider by notNull()
    private val cameraSelector: CameraSelector =
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

    private val multiPermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.entries.size < 1) {
                Toast.makeText(this, "Please Accept all the permissions", Toast.LENGTH_SHORT).show()
            } else {
                viewFinder.post {
                    startCamera()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        permission()
        initListeners()
        initObservers()
    }

    private fun permission() {
        if (allPermissionsGranted()) {
            viewFinder.post {
                startCamera()
            }
        } else {
            multiPermissionCallback.launch(
                REQUIRED_PERMISSIONS
            )
        }
    }

    private fun initObservers() {
        viewModel.resultPoints.observe(this) {
            olActScanner.onCenterChanged(it)
        }

        viewModel.touchCoordinate.observe(this) {
            onTouch(it.x, it.y)
        }

        viewModel.resultText.observe(this) {
            adapter.setData(it)
        }

    }

    private fun initListeners() {
        resultRecyclerView.layoutManager = LinearLayoutManager(this)
        resultRecyclerView.adapter = adapter

        multiPermissionCallback.launch(REQUIRED_PERMISSIONS)

        clearText.setOnClickListener {
            adapter.clear()
        }
    }

    private fun initViews() {
        adapter = BarcodeRecyclerViewAdapter()
        viewFinder = findViewById(R.id.viewFinder)
        clearText = findViewById(R.id.clear_text)
        olActScanner = findViewById(R.id.olActScanner)
        resultRecyclerView = findViewById(R.id.resultRecyclerView)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            try {
                cameraProvider.unbindAll()
                camera =
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
                initCameraZoomable()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initCameraZoomable() {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale = camera.cameraInfo.zoomState.value?.zoomRatio!! * detector.scaleFactor
                camera.cameraControl.setZoomRatio(scale)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this, listener)

        viewFinder.setOnTouchListener { v, event ->
            scaleGestureDetector.onTouchEvent(event)
            viewModel.startAutoFocus(v.x, v.y)
            return@setOnTouchListener true
        }
    }

    private fun onTouch(x: Float, y: Float) {
        val meteringPoint = camera.cameraInfo.let {
            DisplayOrientedMeteringPointFactory(
                viewFinder.display,
                it,
                viewFinder.width.toFloat(),
                viewFinder.height.toFloat()
            ).createPoint(x, y)
        }

        val action = meteringPoint.let { FocusMeteringAction.Builder(it).build() }

        action.let { camera.cameraControl.startFocusAndMetering(it) }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onQRCodeFound(qrCode: Result) {
        viewModel.mapResult(qrCode)
    }

    override fun onManyQRCodeFound(qrCodes: Array<Result>) {
        viewModel.mapResult(*qrCodes)
    }

    override fun qrCodeNotFound() = Unit

    private companion object {
        const val TARGET_PREVIEW_WIDTH = 960
        const val TARGET_PREVIEW_HEIGHT = 1280
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
