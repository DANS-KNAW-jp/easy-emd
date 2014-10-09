package nl.knaw.dans.easy.security.authz;

import nl.knaw.dans.common.lang.repo.DmoStoreId;
import nl.knaw.dans.common.lang.user.User;
import nl.knaw.dans.easy.domain.model.DatasetItemContainer;

public class EasyItemContainerAuthzStrategy extends AbstractItemContainerAuthzStrategy {

    private static final long serialVersionUID = -3903342896601185096L;

    private DatasetItemContainer itemContainer;

    protected EasyItemContainerAuthzStrategy() {
        super();
    }

    protected EasyItemContainerAuthzStrategy(User user, Object target, Object... contextObjects) {
        super(user, contextObjects);
        if (target instanceof DatasetItemContainer) {
            itemContainer = (DatasetItemContainer) target;
        }
        checkAttributes();
    }

    protected EasyItemContainerAuthzStrategy(Object target) {
        if (target instanceof DatasetItemContainer) {
            itemContainer = (DatasetItemContainer) target;
        }
    }

    @Override
    protected void checkAttributes() {
        super.checkAttributes();
        if (itemContainer == null)
            throw new IllegalArgumentException("Insufficient parameters: no itemContainer");
        DmoStoreId datasetId = getDataset().getDmoStoreId();
        if (!(datasetId.equals(itemContainer.getDatasetItemContainerMetadata().getDatasetDmoStoreId()) || datasetId.equals(itemContainer.getDmoStoreId())))
            throw new IllegalArgumentException("ItemContainer is not given dataset, nor part of given dataset");
    }

    @Override
    public boolean canUnitBeDiscovered(String unitId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean canUnitBeRead(String unitId) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    protected boolean canAllBeRead() {
        return TriState.ALL.equals(canChildrenBeRead());
    }

    @Override
    public EasyItemContainerAuthzStrategy newStrategy(User user, Object target, Object... contextObjects) {
        return new EasyItemContainerAuthzStrategy(user, target, contextObjects);
    }

    @Override
    public EasyItemContainerAuthzStrategy sameStrategy(Object target) {
        EasyItemContainerAuthzStrategy sameStrategy = new EasyItemContainerAuthzStrategy(target);
        super.clone(sameStrategy);

        sameStrategy.checkAttributes();
        return sameStrategy;
    }

    @Override
    DmoStoreId getTargetDmoStoreId() {
        return itemContainer.getDmoStoreId();
    }
}
