package tw.idv.poipoi.pdcs_prototype.fragment;

import android.content.Intent;
import android.graphics.Color;
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
import org.dust.capApi.Info;
import org.dust.capApi.Severity;
import tw.idv.poipoi.pdcs_prototype.MapsActivity;
import tw.idv.poipoi.pdcs_prototype.R;
import tw.idv.poipoi.pdcs_prototype.SeverityColor;
import static tw.idv.poipoi.pdcs_prototype.fragment.CapListHandler.isStrikeThrough;
import static tw.idv.poipoi.pdcs_prototype.fragment.CapListHandler.setStrikeThrough;
import static tw.idv.poipoi.pdcs_prototype.fragment.CapListHandler.removeStrikeThrough;

/**
 * Created by DuST on 2017/4/30.
 */

public class InfoListHandler extends BaseAdapter {

    private LayoutInflater inflater;
    private ListView infoList;
    private CAP cap;

    private class ItemHolder {
        RelativeLayout layout;
        ImageView icon;
        TextView title;
        TextView description;

        public ItemHolder(RelativeLayout layout, ImageView icon, TextView title, TextView description) {
            this.layout = layout;
            this.icon = icon;
            this.title = title;
            this.description = description;
        }
    }

    public InfoListHandler(final View rootView,final CAP cap) {
        this.cap = cap;
        inflater = LayoutInflater.from(rootView.getContext());
        infoList = (ListView) rootView.findViewById(R.id.listView_infoList);
        infoList.setAdapter(this);

        int height = infoList.getPaddingTop() + infoList.getPaddingBottom();
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(infoList.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < cap.info.size(); i++) {
            View v = getView(i, null, infoList);
            v.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
            v.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            height += v.getMeasuredHeight();
        }
        Log.d("debug", "TotalHeight: " + height);

        ViewGroup.LayoutParams params = infoList.getLayoutParams();
        params.height = height
                + (infoList.getDividerHeight() * (infoList.getCount() - 1));
        infoList.setLayoutParams(params);
        infoList.requestLayout();

        infoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("debug", "CapId= " + cap.identifier);
                Intent intent = new Intent(rootView.getContext(), MapsActivity.class);
                intent.putExtra("capId", cap.identifier);
                intent.putExtra("infoIndex", position);
                rootView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getCount() {
        return cap.info.size();
    }

    @Override
    public Object getItem(int position) {
        return cap.info.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listitem_info, null);
            holder = new ItemHolder(
                    (RelativeLayout) convertView.findViewById(R.id.info_item_bg),
                    (ImageView) convertView.findViewById(R.id.imageView_icon),
                    (TextView) convertView.findViewById(R.id.textView_title),
                    (TextView) convertView.findViewById(R.id.textView_description)
            );
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        Info info = cap.info.get(position);
        TextView titleText = holder.title;
        titleText.setText(info.event.trim());
        holder.description.setText(info.description.trim());

        AlertStatus status = info.getStatus();
        switch (status){
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

        Severity.SeverityCode actualEffect = info.getActualEffect();
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
}
