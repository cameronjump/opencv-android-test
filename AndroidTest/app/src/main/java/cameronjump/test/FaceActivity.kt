package cameronjump.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceView
import cameronjump.test.R.xml.haarcascade_frontalface_alt
import kotlinx.android.synthetic.main.activity_face.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.core.Core.rotate
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class FaceActivity: AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val TAG = "FaceActivityDebug"
    private val REQUEST_IMAGE_CAPTURE = 200
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private lateinit var mCascadeClassifier: CascadeClassifier
    //private val mClassifierName = haarcascade_frontalface_alt
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

        val cascadeDir = getDir("xml", Context.MODE_PRIVATE)
        var mCascadeFile = File(cascadeDir,"haarcascade_frontalface_alt")
        mCascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)


        loadClassifier()

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

        Imgproc.cvtColor(mRgbaT, mRgbaG, Imgproc.COLOR_RGB2GRAY)
        mCascadeClassifier.detectMultiScale(mRgbaG, faces)
        Log.d(TAG,faces.toString())

        val facesArray = faces.toArray()
        for (face: Rect in facesArray) {
            Imgproc.rectangle(mRgbaT, face.tl(), face.br(), Scalar(0.0, 0.0, 255.0))
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

    fun loadClassifier() {
        try {
            // load cascade file from application resources
            val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
            val cascadeDir = getDir("cascade", Context.MODE_PRIVATE)
            val mCascadeFile = File(cascadeDir, "haarcascade_frontalface_alt.xml")
            val outputStream = FileOutputStream(mCascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int = inputStream.read(buffer)
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = inputStream.read(buffer)
            }
            inputStream.close()
            outputStream.close()

            mCascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)
            if (mCascadeClassifier.empty()) {
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