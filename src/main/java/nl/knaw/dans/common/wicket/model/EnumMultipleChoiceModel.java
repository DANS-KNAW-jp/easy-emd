package nl.knaw.dans.common.wicket.model;

import java.io.Serializable;
import java.util.List;

import nl.knaw.dans.common.wicket.EnumChoiceRenderer;

import org.apache.wicket.model.Model;

/**
 * An example with one of the options initially checked. For more context see
 * {@link EnumChoiceRenderer}
 *
 * <pre>
 *         final List<String> choiceList = Arrays.asList(Foo.values());
 *         final List<Foo> checkedValues = Arrays.asList(new Foo[]{Foo.bar});
 *         add(new CheckBoxMultipleChoice("selectFoo",checkedValues,choiceList,renderer));
 * </pre>
 */
public class EnumMultipleChoiceModel extends Model
{
    private static final long serialVersionUID = 1L;
    private final List<Enum<?>> checkedValues;

    public EnumMultipleChoiceModel(List<Enum<?>> checkedValues)
    {
        this.checkedValues = checkedValues;
    }

    public Serializable getObject()
    {
        return (Serializable) checkedValues;
    }
}
