package pw.brock.mmdn.models;

/**
 * @author BrockWS
 */
public interface IDataModel {

    void prepareForMinify();

    default void populateDefaults() {

    }
}
