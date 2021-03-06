package usbong.android.builder.fragments.screens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.wrapp.floatlabelededittext.FloatLabeledEditText;
import de.greenrobot.event.EventBus;
import rx.Observer;
import usbong.android.builder.R;
import usbong.android.builder.activities.SelectScreenActivity;
import usbong.android.builder.controllers.ScreenDetailController;
import usbong.android.builder.events.OnNeedRefreshScreen;
import usbong.android.builder.events.OnScreenDetailsSave;
import usbong.android.builder.events.OnScreenDetailsSaveError;
import usbong.android.builder.events.OnScreenSave;
import usbong.android.builder.fragments.ScreenDetailFragment;
import usbong.android.builder.fragments.SelectScreenFragment;
import usbong.android.builder.models.Screen;
import usbong.android.builder.models.ScreenRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rocky Camacho on 8/7/2014.
 */
public abstract class BaseScreenFragment extends Fragment {

    private static final String TAG = BaseScreenFragment.class.getSimpleName();
    public static final int ADD_CHILD_REQUEST_CODE = 101;
    protected static final int DELETE_SELECTED_CHILD_REQUEST_CODE = 301;
    public static final String RELATION_CONDITION = "DEFAULT";

    protected long screenId = -1;
    protected long treeId = -1;
    protected Screen currentScreen;

    protected ScreenDetailController controller;

    @InjectView(R.id.name)
    FloatLabeledEditText name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            screenId = getArguments().getLong(ScreenDetailFragment.EXTRA_SCREEN_ID, -1);
            treeId = getArguments().getLong(ScreenDetailFragment.EXTRA_TREE_ID, -1);
        }
        if (screenId == -1) {
            throw new IllegalArgumentException("screen id is required");
        }
        if (treeId == -1) {
            throw new IllegalArgumentException("tree id is required");
        }
        Log.d(TAG, "currentScreen id: " + screenId);
        setHasOptionsMenu(true);
        controller = new ScreenDetailController();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
    }

    protected abstract int getLayoutResId();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.inject(this, view);
        EventBus.getDefault().register(this);

        controller.loadScreen(screenId, new Observer<Screen>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Screen screen) {
                currentScreen = screen;
                onScreenLoad(screen);
            }
        });
    }

    protected abstract void onScreenLoad(Screen screen);

    public void onEvent(OnScreenSave event) {
        try {
            String screenName = name.getText().toString().trim();
            String details = convertFormDataToScreenDetails();
            EventBus.getDefault().post(new OnScreenDetailsSave(screenName, details));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new OnScreenDetailsSaveError(e));
        }
    }

    protected abstract String convertFormDataToScreenDetails() throws Exception;

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.screen_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_child) {
            Intent data = new Intent(getActivity(), SelectScreenActivity.class);
            data.putExtra(SelectScreenFragment.EXTRA_SCREEN_RELATION, SelectScreenFragment.CHILD);
            data.putExtra(SelectScreenFragment.EXTRA_SCREEN_ID, screenId);
            data.putExtra(SelectScreenFragment.EXTRA_TREE_ID, treeId);
            getParentFragment().startActivityForResult(data, ADD_CHILD_REQUEST_CODE);
        }
        if (item.getItemId() == R.id.action_remove_child) {
            Intent data = new Intent(getActivity(), SelectScreenActivity.class);
            data.putExtra(SelectScreenFragment.EXTRA_SCREEN_ID, screenId);
            data.putExtra(SelectScreenFragment.EXTRA_TREE_ID, treeId);
            data.putExtra(SelectScreenFragment.EXTRA_IS_FOR_DELETE_CHILD, true);
            getParentFragment().startActivityForResult(data, DELETE_SELECTED_CHILD_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", Intent data)");
        if (requestCode == ADD_CHILD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            updateChildren(data);
        }
        if (requestCode == DELETE_SELECTED_CHILD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                deleteConnectionWithSelectedScreen(data);
            }
        }
    }

    private void deleteConnectionWithSelectedScreen(Intent data) {
        long childScreenId = data.getLongExtra(SelectScreenFragment.EXTRA_SELECTED_SCREEN_ID, -1);
        controller.loadScreen(childScreenId, new Observer<Screen>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Screen childScreen) {
                deleteRelationWith(childScreen);
            }
        });
    }

    private void deleteRelationWith(Screen childScreen) {
        controller.removeRelation(currentScreen, childScreen, new Observer<Integer>() {
            @Override
            public void onCompleted() {
                EventBus.getDefault().post(OnNeedRefreshScreen.EVENT);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Integer count) {
                if(count > 0) {
                    Toast.makeText(getActivity(), "Screen children removed", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(OnNeedRefreshScreen.EVENT);
                }
                else {
                    Toast.makeText(getActivity(), "This screen has no children.", Toast.LENGTH_SHORT).show();
                    EventBus.getDefault().post(OnNeedRefreshScreen.EVENT);
                }
            }
        });
    }

    protected void updateChildren(Intent data) {
        Log.d(TAG, "resultCode == Activity.RESULT_OK");
        long childScreenId = data.getLongExtra(SelectScreenFragment.EXTRA_SELECTED_SCREEN_ID, -1);

        controller.loadScreen(childScreenId, new Observer<Screen>() {

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Screen childScreen) {
                replaceChildren(childScreen);
            }
        });
    }

    private void replaceChildren(final Screen childScreen) {
        controller.deleteAllChildScreens(currentScreen.getId(), new Observer<Integer>() {
            @Override
            public void onCompleted() {
                saveChildScreen(childScreen);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Integer o) {

            }
        });
    }

    private void saveChildScreen(Screen childScreen) {
        ScreenRelation screenRelation = new ScreenRelation();
        screenRelation.parent = currentScreen;
        screenRelation.child = childScreen;
        screenRelation.condition = RELATION_CONDITION;
        final List<ScreenRelation> screenRelations = new ArrayList<ScreenRelation>();
        screenRelations.add(screenRelation);
        controller.add(screenRelations, new Observer<Object>() {

            @Override
            public void onCompleted() {
                Toast.makeText(getActivity(), "Screen navigation saved", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(OnNeedRefreshScreen.EVENT);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Object o) {

            }
        });
    }


}
