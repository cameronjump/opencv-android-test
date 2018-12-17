package cameronjump.test

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.SurfaceView
import kotlinx.android.synthetic.main.activity_face.*
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Core.rotate
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class FaceActivity: AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private val TAG = "FaceActivityDebug"
    private val REQUEST_IMAGE_CAPTURE = 200
    private lateinit var uri: Uri
    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
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

        face_button.setOnClickListener( {
            startCamera()
        })
    }

    private fun startCamera() {
        //uri = FileProvider.getUriForFile(this, "cameronjump.test.fileprovider",
        //        File.createTempFile("picture", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)))
        //startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }, REQUEST_IMAGE_CAPTURE)
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
        Core.transpose(mRgba,mRgbaT)
        Core.flip(mRgbaT, mRgbaT, -1)
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size())

        return mRgbaT
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //var bitmap = getCapturedImage(uri)
            //bitmap = modifyImage(bitmap)
            //face_image.setImageBitmap(bitmap)


        }
    }

    private fun getCapturedImage(uri: Uri): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        return when (ExifInterface(contentResolver.openInputStream(uri)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(90F) }, true)
            ExifInterface.ORIENTATION_ROTATE_180 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(180F) }, true)
            ExifInterface.ORIENTATION_ROTATE_270 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(270F) }, true)
            else -> bitmap
        }
    }
}