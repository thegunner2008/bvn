package tamhoang.bvn.util.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;

import java.lang.Number;
import java.math.BigDecimal;

import tamhoang.bvn.util.PixelUtil;
import tamhoang.bvn.R;

public class RangeSeekBar<T extends Number> extends androidx.appcompat.widget.AppCompatImageView {
    public static final int ACTION_POINTER_INDEX_MASK = 65280;
    public static final int ACTION_POINTER_INDEX_SHIFT = 8;
    public static final int ACTION_POINTER_UP = 6;
    public static final int DEFAULT_COLOR = Color.argb(255, 51, 181, 229);
    public static final Integer DEFAULT_MAXIMUM = 100;
    public static final Integer DEFAULT_MINIMUM = 0;
    private static final int DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP = 8;
    private static final int DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP = 8;
    private static final int DEFAULT_TEXT_SIZE_IN_DP = 14;
    public static final int HEIGHT_IN_DP = 30;
    private static final int INITIAL_PADDING_IN_DP = 8;
    public static final int INVALID_POINTER_ID = 255;
    public static final int TEXT_LATERAL_PADDING_IN_DP = 3;
    private float INITIAL_PADDING;
    private final int LINE_HEIGHT_IN_DP = 1;
    private T absoluteMaxValue;
    private double absoluteMaxValuePrim;
    private T absoluteMinValue;
    private double absoluteMinValuePrim;
    private OnRangeSeekBarChangeListener<T> listener;
    private int mActivePointerId;
    private int mDistanceToTop;
    private float mDownMotionX;
    private boolean mIsDragging;
    private RectF mRect;
    private int mScaledTouchSlop;
    private boolean mSingleThumb;
    private int mTextOffset;
    private int mTextSize;
    private double normalizedMaxValue;
    private double normalizedMinValue;
    private boolean notifyWhileDragging;
    private NumberType numberType;
    private float padding;
    private final Paint paint;
    private Thumb pressedThumb;
    private final Bitmap thumbDisabledImage;
    private final float thumbHalfHeight;
    private final float thumbHalfWidth;
    private final Bitmap thumbImage;
    private final Bitmap thumbPressedImage;
    private final float thumbWidth;

    /* loaded from: classes.dex */
    public interface OnRangeSeekBarChangeListener<T> {
        void onRangeSeekBarValuesChanged(RangeSeekBar<?> rangeSeekBar, T t, T t2);
    }

    /* loaded from: classes.dex */
    public enum Thumb {
        MIN,
        MAX
    }

    public RangeSeekBar(Context context) {
        super(context);
        this.paint = new Paint(1);
        this.thumbDisabledImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_disabled);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
        this.thumbImage = decodeResource;
        this.thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
        float width = decodeResource.getWidth();
        this.thumbWidth = width;
        this.thumbHalfWidth = width * 0.5f;
        this.thumbHalfHeight = decodeResource.getHeight() * 0.5f;
        this.normalizedMinValue = 0.0d;
        this.normalizedMaxValue = 1.0d;
        this.pressedThumb = null;
        this.notifyWhileDragging = false;
        this.mActivePointerId = 255;
        init(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.paint = new Paint(1);
        this.thumbDisabledImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_disabled);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
        this.thumbImage = decodeResource;
        this.thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
        float width = decodeResource.getWidth();
        this.thumbWidth = width;
        this.thumbHalfWidth = width * 0.5f;
        this.thumbHalfHeight = decodeResource.getHeight() * 0.5f;
        this.normalizedMinValue = 0.0d;
        this.normalizedMaxValue = 1.0d;
        this.pressedThumb = null;
        this.notifyWhileDragging = false;
        this.mActivePointerId = 255;
        init(context, attrs);
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.paint = new Paint(1);
        this.thumbDisabledImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_disabled);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_normal);
        this.thumbImage = decodeResource;
        this.thumbPressedImage = BitmapFactory.decodeResource(getResources(), R.drawable.seek_thumb_pressed);
        float width = decodeResource.getWidth();
        this.thumbWidth = width;
        this.thumbHalfWidth = width * 0.5f;
        this.thumbHalfHeight = decodeResource.getHeight() * 0.5f;
        this.normalizedMinValue = 0.0d;
        this.normalizedMaxValue = 1.0d;
        this.pressedThumb = null;
        this.notifyWhileDragging = false;
        this.mActivePointerId = 255;
        init(context, attrs);
    }

    private T extractNumericValueFromAttributes(TypedArray a, int attribute, int defaultValue) {
        TypedValue tv = a.peekValue(attribute);
        if (tv == null) {
            return (T) Integer.valueOf(defaultValue);
        }
        if (tv.type == 4) {
            return (T) Float.valueOf(a.getFloat(attribute, (float) defaultValue));
        }
        return (T) Integer.valueOf(a.getInteger(attribute, defaultValue));
    }

    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) {
            setRangeToDefaultValues();
        } else {
            TypedArray obtainStyledAttributes = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar, 0, 0);
            setRangeValues(extractNumericValueFromAttributes(obtainStyledAttributes, 1, DEFAULT_MINIMUM), extractNumericValueFromAttributes(obtainStyledAttributes, 0, DEFAULT_MAXIMUM));
            this.mSingleThumb = obtainStyledAttributes.getBoolean(2, false);
            obtainStyledAttributes.recycle();
        }
        setValuePrimAndNumberType();
        this.INITIAL_PADDING = PixelUtil.dpToPx(context, 8);
        this.mTextSize = PixelUtil.dpToPx(context, 14);
        this.mDistanceToTop = PixelUtil.dpToPx(context, 8);
        this.mTextOffset = this.mTextSize + PixelUtil.dpToPx(context, 8) + this.mDistanceToTop;
        float dpToPx = PixelUtil.dpToPx(context, 1) / 2.0f;
        this.mRect = new RectF(this.padding, (this.mTextOffset + this.thumbHalfHeight) - dpToPx, getWidth() - this.padding, this.mTextOffset + this.thumbHalfHeight + dpToPx);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void setRangeValues(T minValue, T maxValue) {
        this.absoluteMinValue = minValue;
        this.absoluteMaxValue = maxValue;
        setValuePrimAndNumberType();
    }

    private void setRangeToDefaultValues() {
        this.absoluteMinValue = (T) DEFAULT_MINIMUM;
        this.absoluteMaxValue = (T) DEFAULT_MAXIMUM;
        setValuePrimAndNumberType();
    }

    private void setValuePrimAndNumberType() {
        this.absoluteMinValuePrim = this.absoluteMinValue.doubleValue();
        this.absoluteMaxValuePrim = this.absoluteMaxValue.doubleValue();
        this.numberType = NumberType.fromNumber(this.absoluteMinValue);
    }

    public void resetSelectedValues() {
        setSelectedMinValue(this.absoluteMinValue);
        setSelectedMaxValue(this.absoluteMaxValue);
    }

    public boolean isNotifyWhileDragging() {
        return this.notifyWhileDragging;
    }

    public void setNotifyWhileDragging(boolean flag) {
        this.notifyWhileDragging = flag;
    }

    public T getAbsoluteMinValue() {
        return this.absoluteMinValue;
    }

    public T getAbsoluteMaxValue() {
        return this.absoluteMaxValue;
    }

    public T getSelectedMinValue() {
        return normalizedToValue(this.normalizedMinValue);
    }

    public void setSelectedMinValue(T value) {
        if (0.0d == this.absoluteMaxValuePrim - this.absoluteMinValuePrim) {
            setNormalizedMinValue(0.0d);
        } else {
            setNormalizedMinValue(valueToNormalized(value));
        }
    }

    public T getSelectedMaxValue() {
        return normalizedToValue(this.normalizedMaxValue);
    }

    public void setSelectedMaxValue(T value) {
        if (0.0d == this.absoluteMaxValuePrim - this.absoluteMinValuePrim) {
            setNormalizedMaxValue(1.0d);
        } else {
            setNormalizedMaxValue(valueToNormalized(value));
        }
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener<T> listener2) {
        this.listener = listener2;
    }

    public boolean onTouchEvent(MotionEvent event) {
        OnRangeSeekBarChangeListener<T> onRangeSeekBarChangeListener;
        if (isEnabled()) {
            int action = event.getAction() & 255;
            if (action == 0) {
                int pointerId = event.getPointerId(event.getPointerCount() - 1);
                this.mActivePointerId = pointerId;
                float x = event.getX(event.findPointerIndex(pointerId));
                this.mDownMotionX = x;
                Thumb evalPressedThumb = evalPressedThumb(x);
                this.pressedThumb = evalPressedThumb;
                if (evalPressedThumb == null) {
                    return super.onTouchEvent(event);
                }
                setPressed(true);
                invalidate();
                onStartTrackingTouch();
                trackTouchEvent(event);
                attemptClaimDrag();
            } else if (action == 1) {
                if (this.mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                this.pressedThumb = null;
                invalidate();
                OnRangeSeekBarChangeListener<T> onRangeSeekBarChangeListener2 = this.listener;
                if (onRangeSeekBarChangeListener2 != null) {
                    onRangeSeekBarChangeListener2.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
            } else if (action != 2) {
                if (action == 3) {
                    if (this.mIsDragging) {
                        onStopTrackingTouch();
                        setPressed(false);
                    }
                    invalidate();
                } else if (action == 5) {
                    int pointerCount = event.getPointerCount() - 1;
                    this.mDownMotionX = event.getX(pointerCount);
                    this.mActivePointerId = event.getPointerId(pointerCount);
                    invalidate();
                } else if (action == 6) {
                    onSecondaryPointerUp(event);
                    invalidate();
                }
            } else if (this.pressedThumb != null) {
                if (this.mIsDragging) {
                    trackTouchEvent(event);
                } else if (Math.abs(event.getX(event.findPointerIndex(this.mActivePointerId)) - this.mDownMotionX) > this.mScaledTouchSlop) {
                    setPressed(true);
                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();
                }
                if (this.notifyWhileDragging && (onRangeSeekBarChangeListener = this.listener) != null) {
                    onRangeSeekBarChangeListener.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue());
                }
            }
            return true;
        }
        return false;
    }

    private final void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & 65280) >> 8;
        if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mDownMotionX = ev.getX(newPointerIndex);
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private final void trackTouchEvent(MotionEvent event) {
        float x = event.getX(event.findPointerIndex(this.mActivePointerId));
        if (Thumb.MIN.equals(this.pressedThumb) && !this.mSingleThumb) {
            setNormalizedMinValue(screenToNormalized(x));
        } else if (Thumb.MAX.equals(this.pressedThumb)) {
            setNormalizedMaxValue(screenToNormalized(x));
        }
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    public void onStartTrackingTouch() {
        this.mIsDragging = true;
    }

    public void onStopTrackingTouch() {
        this.mIsDragging = false;
    }

    @Override // android.widget.ImageView, android.view.View
    public synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = ItemTouchHelper.Callback.DEFAULT_DRAG_ANIMATION_DURATION;
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        int height = this.thumbImage.getHeight() + PixelUtil.dpToPx(getContext(), 30);
        if (View.MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec));
        }
        setMeasuredDimension(width, height);
    }

    public synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setTextSize(this.mTextSize);
        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(-7829368);
        boolean z;
        this.paint.setAntiAlias(true);
        String string = getContext().getString(R.string.demo_min_label);
        String string2 = getContext().getString(R.string.demo_max_label);
        float max = Math.max(this.paint.measureText(string), this.paint.measureText(string2));
        float f = this.mTextOffset + this.thumbHalfHeight + (this.mTextSize / 3);
        canvas.drawText(string, 0.0f, f, this.paint);
        canvas.drawText(string2, getWidth() - max, f, this.paint);
        float f2 = this.INITIAL_PADDING + max + this.thumbHalfWidth;
        this.padding = f2;
        this.mRect.left = f2;
        this.mRect.right = getWidth() - this.padding;
        canvas.drawRect(this.mRect, this.paint);
        z = (getSelectedMinValue().equals(getAbsoluteMinValue()) && getSelectedMaxValue().equals(getAbsoluteMaxValue())) ? false : false;
        int i = z ? -7829368 : DEFAULT_COLOR;
        this.mRect.left = normalizedToScreen(this.normalizedMinValue);
        this.mRect.right = normalizedToScreen(this.normalizedMaxValue);
        this.paint.setColor(i);
        canvas.drawRect(this.mRect, this.paint);
        if (!this.mSingleThumb) {
            drawThumb(normalizedToScreen(this.normalizedMinValue), Thumb.MIN.equals(this.pressedThumb), canvas, z);
        }
        drawThumb(normalizedToScreen(this.normalizedMaxValue), Thumb.MAX.equals(this.pressedThumb), canvas, z);
        if (!z) {
            this.paint.setTextSize(this.mTextSize);
            this.paint.setColor(ViewCompat.MEASURED_STATE_MASK);
            int dpToPx = PixelUtil.dpToPx(getContext(), 3);
            String valueOf = String.valueOf(getSelectedMinValue());
            String valueOf2 = String.valueOf(getSelectedMaxValue());
            float f3 = dpToPx;
            float measureText = this.paint.measureText(valueOf) + f3;
            float measureText2 = this.paint.measureText(valueOf2) + f3;
            if (!this.mSingleThumb) {
                canvas.drawText(valueOf, normalizedToScreen(this.normalizedMinValue) - (measureText * 0.5f), this.mDistanceToTop + this.mTextSize, this.paint);
            }
            canvas.drawText(valueOf2, normalizedToScreen(this.normalizedMaxValue) - (measureText2 * 0.5f), this.mDistanceToTop + this.mTextSize, this.paint);
        }
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("SUPER", super.onSaveInstanceState());
        bundle.putDouble("MIN", this.normalizedMinValue);
        bundle.putDouble("MAX", this.normalizedMaxValue);
        return bundle;
    }

    public void onRestoreInstanceState(Parcelable parcel) {
        Bundle bundle = (Bundle) parcel;
        super.onRestoreInstanceState(bundle.getParcelable("SUPER"));
        this.normalizedMinValue = bundle.getDouble("MIN");
        this.normalizedMaxValue = bundle.getDouble("MAX");
    }

    private void drawThumb(float screenCoord, boolean pressed, Canvas canvas, boolean areSelectedValuesDefault) {
        Bitmap buttonToDraw;
        if (areSelectedValuesDefault) {
            buttonToDraw = this.thumbDisabledImage;
        } else {
            buttonToDraw = pressed ? this.thumbPressedImage : this.thumbImage;
        }
        canvas.drawBitmap(buttonToDraw, screenCoord - this.thumbHalfWidth, (float) this.mTextOffset, this.paint);
    }

    private Thumb evalPressedThumb(float touchX) {
        boolean minThumbPressed = isInThumbRange(touchX, this.normalizedMinValue);
        boolean maxThumbPressed = isInThumbRange(touchX, this.normalizedMaxValue);
        if (minThumbPressed && maxThumbPressed) {
            return touchX / ((float) getWidth()) > 0.5f ? Thumb.MIN : Thumb.MAX;
        } else if (minThumbPressed) {
            return Thumb.MIN;
        } else {
            if (maxThumbPressed) {
                return Thumb.MAX;
            }
            return null;
        }
    }

    private boolean isInThumbRange(float touchX, double normalizedThumbValue) {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= this.thumbHalfWidth;
    }

    private void setNormalizedMinValue(double value) {
        this.normalizedMinValue = Math.max(0.0d, Math.min(1.0d, Math.min(value, this.normalizedMaxValue)));
        invalidate();
    }

    private void setNormalizedMaxValue(double value) {
        this.normalizedMaxValue = Math.max(0.0d, Math.min(1.0d, Math.max(value, this.normalizedMinValue)));
        invalidate();
    }

    private T normalizedToValue(double normalized) {
        double d = this.absoluteMinValuePrim;
        NumberType numberType = this.numberType;
        double round = Math.round((d + ((this.absoluteMaxValuePrim - d) * normalized)) * 100.0d);
        Double.isNaN(round);
        return (T) numberType.toNumber(round / 100.0d);
    }

    private double valueToNormalized(T value) {
        if (0.0d == this.absoluteMaxValuePrim - this.absoluteMinValuePrim) {
            return 0.0d;
        }
        double doubleValue = value.doubleValue();
        double d = this.absoluteMinValuePrim;
        return (doubleValue - d) / (this.absoluteMaxValuePrim - d);
    }

    private float normalizedToScreen(double normalizedCoord) {
        double d = (double) this.padding;
        double width = (double) (((float) getWidth()) - (this.padding * 2.0f));
        Double.isNaN(width);
        Double.isNaN(d);
        return (float) (d + (width * normalizedCoord));
    }

    private double screenToNormalized(float screenCoord) {
        int width = getWidth();
        float f = this.padding;
        if (((float) width) <= f * 2.0f) {
            return 0.0d;
        }
        return Math.min(1.0d, Math.max(0.0d, (double) ((screenCoord - f) / (((float) width) - (f * 2.0f)))));
    }

    /* loaded from: classes.dex */
    public enum NumberType {
        LONG,
        DOUBLE,
        INTEGER,
        FLOAT,
        SHORT,
        BYTE,
        BIG_DECIMAL;

        public static <E extends Number> NumberType fromNumber(E value) throws IllegalArgumentException {
            if (value instanceof Long) {
                return LONG;
            }
            if (value instanceof Double) {
                return DOUBLE;
            }
            if (value instanceof Integer) {
                return INTEGER;
            }
            if (value instanceof Float) {
                return FLOAT;
            }
            if (value instanceof Short) {
                return SHORT;
            }
            if (value instanceof Byte) {
                return BYTE;
            }
            if (value instanceof BigDecimal) {
                return BIG_DECIMAL;
            }
            throw new IllegalArgumentException("Number class '" + value.getClass().getName() + "' is not supported");
        }

        public Number toNumber(double value) {
            switch (AnonymousClass1.$SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[ordinal()]) {
                case 1:
                    return Long.valueOf((long) value);
                case 2:
                    return Double.valueOf(value);
                case 3:
                    return Integer.valueOf((int) value);
                case 4:
                    return Float.valueOf((float) value);
                case 5:
                    return Short.valueOf((short) ((int) value));
                case 6:
                    return Byte.valueOf((byte) ((int) value));
                case 7:
                    return BigDecimal.valueOf(value);
                default:
                    throw new InstantiationError("can't convert " + this + " to a Number object");
            }
        }
    }

    /* loaded from: classes.dex */
    public static class AnonymousClass1 {
        static final int[] $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType;

        static {
            int[] iArr = new int[NumberType.values().length];
            $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType = iArr;
            try {
                iArr[NumberType.LONG.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[NumberType.DOUBLE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[NumberType.INTEGER.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[NumberType.FLOAT.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[NumberType.SHORT.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[NumberType.BYTE.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$tamhoang$ldpro4$RangeSeekBar$NumberType[NumberType.BIG_DECIMAL.ordinal()] = 7;
            } catch (NoSuchFieldError unused7) {
            }
        }
    }
}