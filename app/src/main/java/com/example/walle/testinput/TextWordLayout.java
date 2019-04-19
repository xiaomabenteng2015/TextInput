package com.example.walle.testinput;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 用于文字输入View
 * Created by bobo on 2019-04-18.
 */

public class TextWordLayout extends LinearLayout {

    private final String[] COMMON_PUNCTUATION = new String[] {")", "!", "@", "#", "$", "%", "^", "&", "*", "("};

    private final String[] COMMON_COMMA_PERIOD_ON = new String[] {"<", ">"};
    private final String[] COMMON_COMMA_PERIOD = new String[] {",", "."};

    private final String[] COMMON_GRAVE_SLASH_ON = new String[] {"~", "_", "+", "{", "}", "|", ":", "\"", "?"};
    private final String[] COMMON_GRAVE_SLASH = new String[] {"`", "-", "=", "[", "]", "\\", ";", "'", "/"};

    private int maxLength = 6; //密码长度

    private int inputIndex = 0; //设置子View状态index

    private List<String> mPassList;//储存密码

    private textChangeListener pwdChangeListener;//密码状态改变监听


    private Context mContext;

    private boolean mInputTextEnable;//是否允许输入文字（默认只输入数字）
    private boolean mIsShowInputLine;
    private int mInputColor;
    private int mNoinputColor;
    private int mLineColor;
    private int mTxtInputColor;
    private int mDrawType;
    private int mInterval;
    private int mItemWidth;
    private int mItemHeight;
    private int mShowPassType;
    private int mTxtSize;
    private int mBoxLineSize;


    public void setTextChangeListener(textChangeListener pwdChangeListener) {
        this.pwdChangeListener = pwdChangeListener;
    }

    public TextWordLayout(Context context) {
        this(context, null);
    }

    public TextWordLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextWordLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    /**
     * 初始化View
     */
    private void initView(Context context, AttributeSet attrs) {

        mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PassWordLayoutStyle);

        mInputColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_box_input_color, R.color.pass_view_rect_input);
        mNoinputColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_box_no_input_color, R.color.regi_line_color);
        mLineColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_input_line_color, R.color.pass_view_rect_input);
        mTxtInputColor = ta.getResourceId(R.styleable.PassWordLayoutStyle_text_input_color, R.color.pass_view_rect_input);
        mDrawType = ta.getInt(R.styleable.PassWordLayoutStyle_box_draw_type, 0);
        mInterval = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_interval_width, 4);
        maxLength = ta.getInt(R.styleable.PassWordLayoutStyle_pass_leng, 6);
        mItemWidth = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_item_width, 40);
        mItemHeight = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_item_height, 40);
        mShowPassType = ta.getInt(R.styleable.PassWordLayoutStyle_pass_inputed_type, 0);
        mTxtSize = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_draw_txt_size, 18);
        mBoxLineSize = ta.getDimensionPixelOffset(R.styleable.PassWordLayoutStyle_draw_box_line_size, 4);
        mIsShowInputLine = ta.getBoolean(R.styleable.PassWordLayoutStyle_is_show_input_line, true);
        mInputTextEnable = ta.getBoolean(R.styleable.PassWordLayoutStyle_input_text_enable, false);
        ta.recycle();

        mPassList = new ArrayList<>();

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        //设置点击时弹出输入法
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setFocusable(true);
                setFocusableInTouchMode(true);
                requestFocus();
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(TextWordLayout.this, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        this.setOnKeyListener(new MyKeyListener());//按键监听

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
                    if (passWordView != null) {
                        passWordView.setmIsShowRemindLine(mIsShowInputLine);
                        passWordView.startInputState();
                    }
                } else {
                    PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
                    if (passWordView != null) {
                        passWordView.setmIsShowRemindLine(false);
                        passWordView.updateInputState(false);
                    }
                }
            }
        });
    }

    /**
     * 添加子View
     *
     * @param context
     */
    private void addChildVIews(Context context) {
        for (int i = 0; i < maxLength; i++) {
            PassWordView passWordView = new PassWordView(context);
            LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);
            if (i > 0) {                                       //第一个和最后一个子View不添加边距
                params.leftMargin = mInterval;
            }

            passWordView.setInputStateColor(mInputColor);
            passWordView.setNoinputColor(mNoinputColor);
            passWordView.setInputStateTextColor(mTxtInputColor);
            passWordView.setRemindLineColor(mLineColor);
            passWordView.setmBoxDrawType(mDrawType);
            passWordView.setmShowPassType(mShowPassType);
            passWordView.setmDrawTxtSize(mTxtSize);
            passWordView.setmDrawBoxLineSize(mBoxLineSize);
            passWordView.setmIsShowRemindLine(mIsShowInputLine);

            addView(passWordView, params);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (getChildCount() == 0) {     //判断 子View宽+边距是否超过了父布局 超过了则重置宽高
            //暂时不考虑超过父布局直接使用子view
            addChildVIews(getContext());
        }

    }

    /**
     * 代码设置可填项长度
     *
     * @param maxLength
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (getChildCount() != 0) {
            //判断是否有子布局,清空
            removeAllViewsInLayout();
            //清空所有输入的密码
            removeAllPwd();
        }
        //重新添加
        addChildVIews(getContext());
    }

    /**
     * 添加密码
     *
     * @param pwd
     */
    public void addPwd(String pwd) {
        if (mPassList != null && mPassList.size() < maxLength) {
            int length = mPassList.size() + pwd.length() <= maxLength ? pwd.length() : maxLength - mPassList.size();
            for (int i = 0; i < length; i++) {
                char[] chars = pwd.toCharArray();
                mPassList.add(Character.toString(chars[i]));
                setNextInput(Character.toString(chars[i]));
            }
        }

        if (pwdChangeListener != null) {
            if (mPassList.size() < maxLength) {
                pwdChangeListener.onChange(getPassString());
            } else {
                pwdChangeListener.onFinished(getPassString());
            }
        }
    }

    /**
     * 删除密码
     */
    public void removePwd() {
        if (mPassList != null && mPassList.size() > 0) {
            mPassList.remove(mPassList.size() - 1);
            setPreviosInput();
        }

        if (pwdChangeListener != null) {
            if (mPassList.size() > 0) {
                pwdChangeListener.onChange(getPassString());
            } else {
                pwdChangeListener.onNull();
            }
        }
    }

    /**
     * 清空所有密码
     */
    public void removeAllPwd() {
        if (mPassList != null) {
            for (int i = mPassList.size(); i >= 0; i--) {
                if (i > 0) {
                    setNoInput(i, false, "");
                } else if (i == 0) {
                    PassWordView passWordView = (PassWordView) getChildAt(i);
                    if (passWordView != null) {
                        passWordView.setmPassText("");
                        passWordView.startInputState();
                    }
                }

            }

            mPassList.clear();
            inputIndex = 0;
        }


        if (pwdChangeListener != null) {
            pwdChangeListener.onNull();
        }
    }

    /**
     * 获取密码
     *
     * @return pwd
     */
    public String getPassString() {

        StringBuffer passString = new StringBuffer();

        for (String i : mPassList) {
            passString.append(i);
        }

        return passString.toString();
    }

    /**
     * 设置下一个View为输入状态
     */
    private void setNextInput(String pwdTxt) {
        if (inputIndex < maxLength) {
            setNoInput(inputIndex, true, pwdTxt);
            inputIndex++;
            PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
            if (passWordView != null) {
                passWordView.setmPassText(pwdTxt + "");
                passWordView.startInputState();
            }
        }

    }

    /**
     * 设置上一个View为输入状态
     */
    private void setPreviosInput() {
        if (inputIndex > 0) {
            setNoInput(inputIndex, false, "");
            inputIndex--;
            PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
            if (passWordView != null) {
                passWordView.setmPassText("");
                passWordView.startInputState();
            }
        } else if (inputIndex == 0) {
            PassWordView passWordView = (PassWordView) getChildAt(inputIndex);
            if (passWordView != null) {
                passWordView.setmPassText("");
                passWordView.startInputState();
            }
        }
    }

    /**
     * 设置指定View为不输入状态
     *
     * @param index   view下标
     * @param isinput 是否输入过密码
     */
    public void setNoInput(int index, boolean isinput, String txt) {
        if (index < 0) {
            return;
        }
        PassWordView passWordView = (PassWordView) getChildAt(index);
        if (passWordView != null) {
            passWordView.setmPassText(txt);
            passWordView.updateInputState(isinput);
        }
    }


    public interface textChangeListener {
        void onChange(String pwd);//密码改变

        void onNull();  //密码删除为空

        void onFinished(String pwd);//密码长度已经达到最大值
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (mInputTextEnable){
            outAttrs.inputType = InputType.TYPE_CLASS_TEXT;          //显示拼音
        } else {
            outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;          //显示数字
        }
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return new ZanyInputConnection(this, false);
    }


    private class ZanyInputConnection extends BaseInputConnection {

        @Override
        public boolean commitText(CharSequence txt, int newCursorPosition) {
            return super.commitText(txt, newCursorPosition);
        }

        public ZanyInputConnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return super.sendKeyEvent(event);
        }


        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            if (beforeLength == 1 && afterLength == 0) {
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }


    /**
     * 按键监听器
     */
    class MyKeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return false;
                }

                if (!mInputTextEnable && event.isShiftPressed()){
                    //如果输入文本类型只为数字，则可以执行到这里
                    //如果输入文本类型为汉字、英文等，则可以不执行这里
                    return true;
                }

                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    if (event.getMetaState() == (KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON)){
                        //电脑键盘上数字对应的标点
                        addPwd(COMMON_PUNCTUATION[keyCode - 7]);
                    } else {
                        //处理数字
                        addPwd(keyCode - 7 + "");
                    }
                    return true;
                }

                if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z){//英文字母29 - 54
                    char ch;
                    if (event.getMetaState() == (KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON)){
                        //字母大写
                        ch = (char) (keyCode + 36);
                    }else {
                        ch = (char) (keyCode + 36 + 32);
                    }
                    addPwd(Character.toString(ch));
                    return true;
                }

                if (keyCode >= KeyEvent.KEYCODE_COMMA && keyCode <= KeyEvent.KEYCODE_PERIOD){
                    if (event.getMetaState() == (KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON)){
                        addPwd(COMMON_COMMA_PERIOD_ON[keyCode-55]);// < >
                    } else {
                        addPwd(COMMON_COMMA_PERIOD[keyCode-55]);// . ,
                    }
                    return true;
                }

                if (keyCode >= KeyEvent.KEYCODE_GRAVE && keyCode <= KeyEvent.KEYCODE_SLASH){
                    if (event.getMetaState() == (KeyEvent.META_SHIFT_ON | KeyEvent.META_SHIFT_LEFT_ON)){
                        addPwd(COMMON_GRAVE_SLASH_ON[keyCode-68]);
                    } else {
                        addPwd(COMMON_GRAVE_SLASH[keyCode-68]);
                    }
                    return true;
                }

                if (event.isShiftPressed()){
                    return true;
                }

                if (keyCode == KeyEvent.KEYCODE_DEL) {       //点击删除
                    removePwd();
                    return true;
                }

                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            } else if (action == KeyEvent.ACTION_MULTIPLE){
                if (!mInputTextEnable && event.isShiftPressed()){
                    //如果输入文本类型只为数字，则可以执行到这里
                    //如果输入文本类型为汉字、英文等，则可以不执行这里
                    return true;
                }

                String characters = event.getCharacters();
                if (!TextUtils.isEmpty(characters)){
                    if (characters.equals("·") || characters.equals("¥") || characters.equals("€")){
                        addPwd(characters);
                        return true;
                    }
                }
                if (!TextUtils.isEmpty(characters) && !isEmoji(characters)){
                    //点击添加中文
                    addPwd(characters);
                    return true;
                }
            } else {
                //UP事件

            }
            return false;
        }
    }


    //恢复状态
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        this.mPassList = savedState.saveString;
        inputIndex = mPassList.size();
        if (mPassList.isEmpty()) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            PassWordView passWordView = (PassWordView) getChildAt(i);
            if (i > mPassList.size() - 1) {
                if (passWordView != null) {
                    passWordView.setmIsShowRemindLine(false);
                    passWordView.updateInputState(false);
                }
                break;
            }

            if (passWordView != null) {
                passWordView.setmPassText(mPassList.get(i));
                passWordView.updateInputState(true);
            }
        }

    }

    //保存状态
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.saveString = this.mPassList;
        return savedState;
    }


    public static class SavedState extends BaseSavedState {
        public List<String> saveString;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeList(saveString);
        }

        private SavedState(Parcel in) {
            super(in);
            in.readStringList(saveString);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    /**
     * Emoji表情校验
     *
     * @param string
     * @return
     */
    public boolean isEmoji(String string) {
        //过滤Emoji表情
//        Pattern p = Pattern.compile("[^\\u0000-\\uFFFF]");
        //过滤Emoji表情和颜文字
        Pattern p = Pattern.compile("[\\ud83c\\udc00-\\ud83c\\udfff]|[\\ud83d\\udc00-\\ud83d\\udfff]|[\\u2600-\\u27ff]|[\\ud83e\\udd00-\\ud83e\\uddff]|[\\u2300-\\u23ff]|[\\u2500-\\u25ff]|[\\u2100-\\u21ff]|[\\u0000-\\u00ff]|[\\u2b00-\\u2bff]|[\\u2d06]|[\\u3030]");
        Matcher m = p.matcher(string);
        return m.find();

    }
}
