package sg.edu.nus.comp.lms.domain.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class JsonMarshaller implements Marshaller {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            if (bytes == null) {
                return null;
            }
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public <T extends Collection<E>, E> T deserializeCollection(byte[] bytes, Class<T> cClass, Class<E> eClass) {
        try {
            if (bytes == null) {
                return null;
            }
            TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
            CollectionType mapType = typeFactory.constructCollectionType(cClass, eClass);
            return OBJECT_MAPPER.readValue(bytes, mapType);
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    @Override
    public <M extends Map<K, V>, K, V> M deserializeMap(byte[] bytes, Class<M> mClass, Class<K> kClass, Class<V> vClass) {
        try {
            if (bytes == null) {
                return null;
            }
            TypeFactory typeFactory = OBJECT_MAPPER.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(mClass, kClass, vClass);
            return OBJECT_MAPPER.readValue(bytes, mapType);
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed", e);
        }
    }
}