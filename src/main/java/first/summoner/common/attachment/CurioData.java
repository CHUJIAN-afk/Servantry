package first.summoner.common.attachment;

import first.summoner.common.item.CurioItem;

import java.util.Set;

public record CurioData(Set<CurioItem> curioItems) {

    public boolean isEquipped(CurioItem item) {
        return curioItems.contains(item);
    }

    public Set<CurioItem> getCurioItemList() {
        return curioItems;
    }
}
