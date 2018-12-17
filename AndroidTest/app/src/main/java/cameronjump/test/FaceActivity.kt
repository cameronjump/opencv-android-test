package cameronjump.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceView
import kotlinx.android.synthetic.main.activity_face.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FaceActivity: AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val TAG = "FaceActivityDebug"
    private val REQUEST_IMAGE_CAPTURE = 200
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private lateinit var faceDetector: CascadeClassifier
    private lateinit var eyeDetector: CascadeClassifier
    private val init = initialization()

    class initialization {
        companion object {
            init {
                OpenCVLoader.initDebug()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face)

        mOpenCvCameraView = face_image as CameraBridgeViewBase
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE)
        mOpenCvCameraView.setCvCameraViewListener(this)
        mOpenCvCameraView.setCameraIndex(1)

        faceDetector = CascadeClassifier()
        loadClassifier(faceDetector, R.raw.haarcascade_frontalface_alt, "haarcascade_frontalface_alt.xml")

        eyeDetector = CascadeClassifier()
        loadClassifier(eyeDetector, R.raw.haarcascade_eye, "haarcascade_eye.xml")

    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView()
    }

    public override fun onResume() {
        super.onResume()
        mOpenCvCameraView.enableView()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
    }

    override fun onCameraViewStopped() {}

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        var mRgba = inputFrame.rgba()
        var mRgbaT = Mat()
        var mRgbaG = Mat()
        Core.transpose(mRgba,mRgbaT)
        Core.flip(mRgbaT, mRgbaT, -1)
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size())

        var faces = MatOfRect()
        val eyes = MatOfRect()

        Imgproc.cvtColor(mRgbaT, mRgbaG, Imgproc.COLOR_RGB2GRAY)
        faceDetector.detectMultiScale(mRgbaG, faces)
        eyeDetector.detectMultiScale(mRgbaG, eyes)

        val facesArray = faces.toArray()
        for (face: Rect in facesArray) {
            Imgproc.rectangle(mRgbaT, face.tl(), face.br(), Scalar(0.0, 0.0, 255.0), 3)
        }

        val eyesArray = eyes.toArray()
        for (eye: Rect in eyesArray) {
            Imgproc.rectangle(mRgbaT, eye.tl(), eye.br(), Scalar(0.0, 255.0, 0.0), 3)
        }


        return mRgbaT
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //var bitmap = getCapturedImage(uri)
            //bitmap = modifyImage(bitmap)
            //face_image.setImageBitmap(bitmap)


        }
    }

    fun loadClassifier(detector: CascadeClassifier, fileID: Int, fileName: String) {
        try {
            // load cascade file from application resources
            val inputStream = resources.openRawResource(fileID)
            val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
            val mCascadeFile = File(cascadeDir, fileName)
            val outputStream = FileOutputStream(mCascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int = inputStream.read(buffer)
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }
            inputStream.close()
            outputStream.close()

            detector.load(mCascadeFile.absolutePath)
            if (detector.empty()) {
                Log.d(TAG, "Failed to load cascade classifier")
            } else
                Log.d(TAG, "Loaded cascade classifier from " + mCascadeFile.absolutePath)


            cascadeDir.delete()

        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Failed to load cascade. Exception thrown: $e")
        }
    }
}