package anghami.com.diffingadapterexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static String[] descriptions = {
            "Lorem ipsum dolor sit amet",
            "Sale reprimique his eu",
            "At sint veniam menandri cum",
            "Cum eu recusabo efficiendi",
            "Eu quot deterruisset cum",
            "Et latine blandit pro",
            "Ne noster melius eruditi nam",
            "Essent laboramus ei vel",
            "vel natum prima ipsum ex",
            "Mea docendi suavitate ea",
            "Cu pro falli ocurreret scriptorem",
            "Te mea mazim dolores lucilius",
            "Ea viris facete referrentur vix",
            "Ex verear fierent forensibus pri",
            "Ad fierent erroribus deseruisse usu",
            "At tollit quidam posidonium sea",
            "Id aeterno omnesque instructior pro",
            "Dicam populo nec et",
            "Vim animal eleifend scripserit te",
            "Ea paulo docendi petentium cum"
    };

    private List<DataItem> mData;
    private DiffingAdapter mAdapter;
    private Random mRandom = new Random();
    private int idCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new DiffingAdapter();
        recyclerView.setAdapter(mAdapter);

        mData = new ArrayList<>(descriptions.length);
        for(String description: descriptions) {
            mData.add(new DataItem(nextId(), description));
        }
    }

    private String nextId() {
        idCounter++;
        return String.valueOf(idCounter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.refresh(mData);
    }

    private void moveOne() {
        if (mData == null) {
            return;
        }

        int fromIdx = mRandom.nextInt(mData.size());
        DataItem item = mData.remove(fromIdx);
        int toIdx = mRandom.nextInt(mData.size()+1);
        mData.add(toIdx, item);

        mAdapter.refresh(mData);
    }

    private void removeOne() {
        if (mData == null) {
            return;
        }

        int idx = mRandom.nextInt(mData.size());
        mData.remove(idx);
        mAdapter.refresh(mData);
    }

    private void addOne() {
        if (mData == null) {
            return;
        }

        String description = descriptions[mRandom.nextInt(descriptions.length)];
        DataItem item = new DataItem(nextId(), description);
        int idx = mRandom.nextInt(mData.size()+1);
        mData.add(idx, item);
        mAdapter.refresh(mData);
    }

    private void modifyOne() {
        if (mData == null) {
            return;
        }

        String description = descriptions[mRandom.nextInt(descriptions.length)];
        int idx = mRandom.nextInt(mData.size());
        mData.get(idx).setDescription(description);
        mAdapter.refresh(mData);
    }

    public void doSomething(View v) {
        switch (mRandom.nextInt(4)) {
            case 0:
                moveOne();
                break;
            case 1:
                removeOne();
                break;
            case 2:
                addOne();
                break;
            case 3:
                modifyOne();
                break;
        }
    }
}
