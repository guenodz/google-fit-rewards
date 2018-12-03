package me.guendouz.googlefithistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.RewardViewHolder> {

    // we need the current user steps so we can check if he's eligible for rewards
    private int currentUserSteps;
    private List<Reward> rewardList;

    public RewardsAdapter(int currentUserSteps, List<Reward> rewardList) {
        this.currentUserSteps = currentUserSteps;
        this.rewardList = rewardList;
    }

    public void setCurrentUserSteps(int currentUserSteps) {
        this.currentUserSteps = currentUserSteps;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.reward_item, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        if (rewardList != null && rewardList.size() > 0)
            holder.bind(rewardList.get(position));
    }

    @Override
    public int getItemCount() {
        return rewardList == null ? 0 : rewardList.size();
    }

    public class RewardViewHolder extends RecyclerView.ViewHolder {

        private TextView tvRewardName, tvRewardSteps;
        private ImageView ivRewardImage;
        private MaterialButton btnBuyReward;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRewardName = itemView.findViewById(R.id.tvRewardName);
            tvRewardSteps = itemView.findViewById(R.id.tvRewardSteps);
            ivRewardImage = itemView.findViewById(R.id.ivReward);
            btnBuyReward = itemView.findViewById(R.id.btnBuyReward);
        }

        public void bind(Reward reward) {
            if (reward != null) {
                tvRewardName.setText(reward.getName());
                tvRewardSteps.setText(String.format("%d Pts", reward.getSteps()));
                ivRewardImage.setImageResource(reward.getImageId());
                //  if the reward steps is greater than current user steps, the user can't get the reward!
                if (reward.getSteps() > currentUserSteps) {
                    btnBuyReward.setEnabled(false);
                }
            }
        }
    }
}
