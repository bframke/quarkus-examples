package kubernetes.client.common;
public  class Holder<T> {
    T object;
    public Holder(T obj) { object = obj; }
    public T get() { return object;}
    public void set(T obj) { object = obj; }
}
