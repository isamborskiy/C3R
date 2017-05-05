package sg.edu.nus.comp.lms.domain.serialization;

import java.util.Collection;
import java.util.Map;

public interface Marshaller {

    /**
     * @return serialized object or {@code null} if object is {@code null}
     */
    byte[] serialize(Object object);

    /**
     * @return deserialized object or {@code null} if bytes is {@code null}
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

    /**
     * @return deserialized collection or {@code null} if bytes is {@code null}
     */
    <T extends Collection<E>, E> T deserializeCollection(byte[] bytes, Class<T> cClass, Class<E> eClass);

    /**
     * @return deserialized map or {@code null} if bytes is {@code null}
     */
    <M extends Map<K, V>, K, V> M deserializeMap(byte[] bytes, Class<M> mClass, Class<K> kClass, Class<V> vClass);
}
