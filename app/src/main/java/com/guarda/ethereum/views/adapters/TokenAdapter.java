package com.guarda.ethereum.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.models.items.TokenBodyItem;
import com.guarda.ethereum.models.items.TokenHeaderItem;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import static android.view.animation.Animation.RELATIVE_TO_SELF;
import static com.guarda.ethereum.models.constants.Common.ETH_SHOW_PATTERN;

public class TokenAdapter extends ExpandableRecyclerViewAdapter<TokenAdapter.TokenHeaderViewHolder, TokenAdapter.TokenBodyViewHolder> {

    private String tokensSum;

    public TokenAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public TokenHeaderViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_token_header, parent, false);
        return new TokenHeaderViewHolder(view);
    }

    @Override
    public TokenBodyViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_token_body, parent, false);
        return new TokenBodyViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(TokenBodyViewHolder holder, int flatPosition,
                                      ExpandableGroup group, int childIndex) {

        final TokenBodyItem token = ((TokenHeaderItem) group).getItems().get(childIndex);
        holder.setTokenName(token.getTokenName());
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat(ETH_SHOW_PATTERN, symbols);

        if (token.getTokenNum().compareTo(new BigDecimal("0.00001")) < 0 &&
                !(token.getTokenNum().compareTo(BigDecimal.ZERO) == 0)) {
            holder.setTokenNum("~" + decimalFormat.format(token.getTokenNum()));
        } else {
            holder.setTokenNum(decimalFormat.format(token.getTokenNum()));
        }

        SharedManager sharedManager = new SharedManager();
        String otherSum = "...";
        if (token.getOtherSum() != 0.0) {
            otherSum = token.getOtherSum() >= 0? Double.toString(round(token.getOtherSum(), 2)): "- ";
        }
        otherSum = String.format("%s %s", otherSum, sharedManager.getLocalCurrency().toUpperCase());
        holder.setTokenSum(otherSum);

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onBindGroupViewHolder(TokenHeaderViewHolder holder, int flatPosition,
                                      ExpandableGroup group) {

        holder.setTvTokenHeaderTitle(group);
        holder.setTokensSum(((TokenHeaderItem) group).getAllTokensSum());
        if (tokensSum != null) holder.setTokensSum(tokensSum);
        if (group.getItemCount() > 0) {
            holder.arrow.setImageDrawable(holder.context.getResources().getDrawable(R.drawable.ic_arrow_down));
        } else {
            holder.arrow.setImageDrawable(holder.context.getResources().getDrawable(R.drawable.ic_arrow_down_grey));
        }
    }

    public void setTokensSum(String tokensSum) {
        this.tokensSum = tokensSum;
    }

    public class TokenHeaderViewHolder extends GroupViewHolder {

        public ImageView arrow;
        private TextView tvTokenHeaderTitle;
        private TextView tvTokenSum;
        public Context context;

        private TokenHeaderViewHolder(View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            arrow = (ImageView) itemView.findViewById(R.id.list_item_expandable_arrow);
            tvTokenHeaderTitle = (TextView) itemView.findViewById(R.id.tv_token_header_title);
            tvTokenSum = (TextView) itemView.findViewById(R.id.tv_all_tokens_sum);
        }

        public void setTokensSum(String sum) {
            tvTokenSum.setText(sum);
        }

        public void setTvTokenHeaderTitle(ExpandableGroup header) {
            tvTokenHeaderTitle.setText(header.getTitle());
        }

        @Override
        public void expand() {
            animateExpand();
        }

        @Override
        public void collapse() {
            animateCollapse();
        }

        private void animateExpand() {
            RotateAnimation rotate =
                    new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }

        private void animateCollapse() {
            RotateAnimation rotate =
                    new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(300);
            rotate.setFillAfter(true);
            arrow.setAnimation(rotate);
        }
    }

    public class TokenBodyViewHolder extends ChildViewHolder {

        private TextView tvTokenName;
        private TextView tvTokenNum;
        private TextView tvTokenSum;

        private TokenBodyViewHolder(View itemView) {
            super(itemView);
            tvTokenName = (TextView) itemView.findViewById(R.id.tv_token_name);
            tvTokenNum = (TextView) itemView.findViewById(R.id.tv_token_num);
            tvTokenSum = (TextView) itemView.findViewById(R.id.tv_token_sum);
        }

        public void setTokenName(String name) {
            tvTokenName.setText(name);
        }

        public void setTokenNum(String num) {
            tvTokenNum.setText(num);
        }

        public void setTokenSum(String sum) {
            tvTokenSum.setText(sum);
        }
    }
}
