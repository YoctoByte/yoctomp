package yoctobyte.yoctomp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalMusicFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LocalMusicFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    static final int CHOOSE_DIRECTORY_REQUEST = 42;

    public LocalMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the main; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.local_music, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.localMusic_addSource) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, CHOOSE_DIRECTORY_REQUEST);
            return true;
        } else if (id == R.id.localMusic_deleteDatabase) {
            getActivity().deleteDatabase("db_yoctomp");
            return true;
        } else if (id == R.id.localMusic_manageSources) {
        return true;
    }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == CHOOSE_DIRECTORY_REQUEST) {
            Uri treeUri = resultData.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);

            TracksDatabase db = new TracksDatabase(getActivity());
            TracksDatabase.TrackTable localMusicTable = db.getTable(TracksDatabase.getTableNameLocalMusic());

            for (DocumentFile file: pickedDir.listFiles()) {
                Log.d("onActivityResult", "name: " + file.getUri() + ", type: " + file.getType() + ", size: " + file.length());
                Track track = new Track(file.getUri());
                localMusicTable.addTrack(track);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_local_music, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}