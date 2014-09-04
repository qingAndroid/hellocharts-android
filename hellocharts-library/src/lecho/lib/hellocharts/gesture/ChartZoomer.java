package lecho.lib.hellocharts.gesture;

import lecho.lib.hellocharts.ChartComputator;
import lecho.lib.hellocharts.model.Viewport;
import android.content.Context;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * Encapsulates zooming functionality.
 * 
 * @author Leszek Wach
 * 
 */
public class ChartZoomer {
	public static final int ZOOM_HORIZONTAL_AND_VERTICAL = 1;
	public static final int ZOOM_HORIZONTAL = 2;
	public static final int ZOOM_VERTICAL = 3;
	public static final float ZOOM_AMOUNT = 0.25f;
	private ZoomerCompat zoomer;
	private int zoomType;
	private PointF zoomFocalPoint = new PointF();// Used for double tap zoom
	private PointF viewportFocus = new PointF();
	private Viewport scrollerStartViewport = new Viewport(); // Used only for zooms and flings

	public ChartZoomer(Context context, int zoomType) {
		zoomer = new ZoomerCompat(context);
		this.zoomType = zoomType;
	}

	public boolean startZoom(MotionEvent e, ChartComputator computator) {
		zoomer.forceFinished(true);
		scrollerStartViewport.set(computator.getCurrentViewport());
		if (!computator.rawPixelsToDataPoint(e.getX(), e.getY(), zoomFocalPoint)) {
			// Focus point is not within content area.
			return false;
		}
		zoomer.startZoom(ZOOM_AMOUNT);
		return true;
	}

	public boolean startZoom(float x, float y, float zoom, ChartComputator computator) {
		zoomer.forceFinished(true);
		scrollerStartViewport.set(computator.getCurrentViewport());
		zoomFocalPoint.set(x, y);
		zoomer.startZoom(zoom);
		return true;
	}

	public boolean computeZoom(ChartComputator computator) {
		if (zoomer.computeZoom()) {
			// Performs the zoom since a zoom is in progress (either programmatically or via
			// double-touch).
			final float newWidth = (1.0f - zoomer.getCurrZoom()) * scrollerStartViewport.width();
			final float newHeight = (1.0f - zoomer.getCurrZoom()) * scrollerStartViewport.height();
			final float pointWithinViewportX = (zoomFocalPoint.x - scrollerStartViewport.left)
					/ scrollerStartViewport.width();
			final float pointWithinViewportY = (zoomFocalPoint.y - scrollerStartViewport.bottom)
					/ scrollerStartViewport.height();

			float left = zoomFocalPoint.x - newWidth * pointWithinViewportX;
			float top = zoomFocalPoint.y + newHeight * (1 - pointWithinViewportY);
			float right = zoomFocalPoint.x + newWidth * (1 - pointWithinViewportX);
			float bottom = zoomFocalPoint.y - newHeight * pointWithinViewportY;
			setCurrentViewport(computator, left, top, right, bottom);
			return true;
		}
		return false;
	}

	public boolean scale(ChartComputator computator, float focusX, float focusY, float scale) {
		/**
		 * Smaller viewport means bigger zoom so for zoomIn scale should have value <1, for zoomOout >1
		 */
		final float newWidth = scale * computator.getCurrentViewport().width();
		final float newHeight = scale * computator.getCurrentViewport().height();
		if (!computator.rawPixelsToDataPoint(focusX, focusY, viewportFocus)) {
			// Focus point is not within content area.
			return false;
		}

		float left = viewportFocus.x - (focusX - computator.getContentRect().left)
				* (newWidth / computator.getContentRect().width());
		float top = viewportFocus.y + (focusY - computator.getContentRect().top)
				* (newHeight / computator.getContentRect().height());
		float right = left + newWidth;
		float bottom = top - newHeight;
		setCurrentViewport(computator, left, top, right, bottom);
		return true;
	}

	private void setCurrentViewport(ChartComputator computator, float left, float top, float right, float bottom) {
		Viewport currentViewport = computator.getCurrentViewport();
		if (zoomType == ZOOM_HORIZONTAL_AND_VERTICAL) {
			computator.setCurrentViewport(left, top, right, bottom);
		} else if (zoomType == ZOOM_HORIZONTAL) {
			computator.setCurrentViewport(left, currentViewport.top, right, currentViewport.bottom);
		} else if (zoomType == ZOOM_VERTICAL) {
			computator.setCurrentViewport(currentViewport.left, top, currentViewport.right, bottom);
		}
	}

	public int getZoomType() {
		return zoomType;
	}

	public void setZoomType(int zoomType) {
		this.zoomType = zoomType;
	}
}
