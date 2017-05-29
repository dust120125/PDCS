package tw.idv.poipoi.pdcs.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.dust.capApi.AlertStatus;
import org.dust.capApi.CAP;
import org.dust.capApi.Severity;

import java.util.ArrayList;

import tw.idv.poipoi.pdcs.MapsActivity;
import tw.idv.poipoi.pdcs.R;
import tw.idv.poipoi.pdcs.SeverityColor;

import static tw.idv.poipoi.pdcs.Core.CORE;

/**
 * Created by DuST on 2017/4/9.
 */

public class CapListHandler extends BaseAdapter {

    private LayoutInflater inflater;
    private ListView mListView;

    private static final int CAP_ID_TAG = 10;
    private ArrayList<CAP> mCapList;

    private class ItemHolder {
        RelativeLayout layout;
        ImageView icon;
        TextView title;
        TextView description;
        ListView infoList;
        ImageView expandIcon;

        public ItemHolder(RelativeLayout layout, ImageView icon, TextView title, TextView description, ListView infoList, ImageView expandIcon) {
            this.layout = layout;
            this.icon = icon;
            this.title = title;
            this.description = description;
            this.infoList = infoList;
            this.expandIcon = expandIcon;
        }
    }

    public CapListHandler() {
        mCapList = new ArrayList<>(CORE.getCapList());
    }

    public CapListHandler(final ListView list) {
        inflater = LayoutInflater.from(list.getContext());
        list.setAdapter(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CAP target = CORE.getCapByIndex(position);
                if (target.info.size() == 1) {
                    Log.d("debug", "CapId= " + target.identifier);
                    Intent intent = new Intent(list.getContext(), MapsActivity.class);
                    intent.putExtra("capId", target.identifier);
                    intent.putExtra("infoIndex", 0);
                    list.getContext().startActivity(intent);
                }
            }
        });
        mListView = list;
    }

    public void setParentList(final ListView list) {
        inflater = LayoutInflater.from(list.getContext());
        list.setAdapter(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CAP target = CORE.getCapByIndex(position);
                if (target == null) return;
                AlertStatus alertStatus = target.getStatus();
                if (target.info.size() == 1) {
                    Log.d("debug", "CapId= " + target.identifier);
                    Intent intent = new Intent(list.getContext(), MapsActivity.class);
                    intent.putExtra("capId", target.identifier);
                    intent.putExtra("infoIndex", 0);
                    list.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getCount() {
        return mCapList.size();
    }

    @Override
    public Object getItem(int position) {
        return mCapList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_cap, null);
            holder = new ItemHolder(
                    (RelativeLayout) convertView.findViewById(R.id.cap_item_bg),
                    (ImageView) convertView.findViewById(R.id.imageView_icon),
                    (TextView) convertView.findViewById(R.id.textView_title),
                    (TextView) convertView.findViewById(R.id.textView_description),
                    (ListView) convertView.findViewById(R.id.listView_infoList),
                    (ImageView) convertView.findViewById(R.id.imageView_capList_expand)
            );
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        CAP cap = CORE.getCapByIndex(position);
        if (cap == null){
            notifyDataSetChanged();
            return convertView;
        }
        holder.title.setText(cap.info.get(0).event.trim());
        holder.infoList.setVisibility(View.GONE);

        if (cap.info.size() > 1) {
            holder.description.setText(cap.info.size() + "項事件，展開以查看");
            holder.infoList.setAdapter(new InfoListHandler(convertView, cap));
            holder.expandIcon.setOnClickListener(new ExtendableOnClickListener(holder) {
                @Override
                public void onClick(View v) {
                    ItemHolder holder = (ItemHolder) getParameter(0);
                    if (holder.infoList.getVisibility() == View.VISIBLE) {
                        holder.infoList.setVisibility(View.GONE);
                    } else {
                        holder.infoList.setVisibility(View.VISIBLE);
                    }
                }
            });
            holder.expandIcon.setVisibility(View.VISIBLE);
        } else {
            holder.description.setText(cap.info.get(0).description.trim());
            holder.expandIcon.setVisibility(View.GONE);
        }
        Log.d("debug", "CAP Infos: " + cap.info.size());

        AlertStatus status = cap.getStatus();
        TextView titleText = holder.title;
        switch (status) {
            case EXPIRED:
                holder.layout.setBackgroundColor(Color.parseColor(Severity.COLOR_NOT_EFFECT));
                if (!isStrikeThrough(titleText)) setStrikeThrough(titleText);
                titleText.setText(titleText.getText() + " (已過期)");
                break;
            case NOT_YET:
                holder.layout.setBackgroundColor(Color.parseColor(Severity.COLOR_NOT_EFFECT));
                if (isStrikeThrough(titleText)) removeStrikeThrough(titleText);
                break;
            case EFFECTIVE:
                holder.layout.setBackgroundColor(Color.WHITE);
                if (isStrikeThrough(titleText)) removeStrikeThrough(titleText);
                break;
        }
        Severity.SeverityCode actualEffect = cap.getActualEffect();
        if (actualEffect != null) {
            switch (actualEffect) {
                case None:
                    holder.title.setTextColor(Color.DKGRAY);
                    break;
                default:
                    holder.title.setTextColor(SeverityColor.getSeverityTextColor(actualEffect));
            }
        }

        convertView.setTag(R.id.tag_capId, cap.identifier);

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        mCapList = new ArrayList<>(CORE.getCapList());
        Log.i("Cap List", "notifyDataSetChanged");
        super.notifyDataSetChanged();
    }

    public static boolean isStrikeThrough(TextView view) {
        return (view.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG)
                == Paint.STRIKE_THRU_TEXT_FLAG;
    }

    public static void setStrikeThrough(TextView view) {
        view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public static void removeStrikeThrough(TextView view) {
        view.setPaintFlags(view.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

}