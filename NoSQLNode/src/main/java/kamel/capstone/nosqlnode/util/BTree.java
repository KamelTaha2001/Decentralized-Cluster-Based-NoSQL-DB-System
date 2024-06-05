package kamel.capstone.nosqlnode.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BTree implements Serializable {

    private final int t;

    private class Node implements Serializable {
        int n;
        Key[] keys = new Key[2 * t - 1];
        Node[] children = new Node[2 * t];
        boolean leaf = true;

        private Node() {
            for (int i = 0; i < 2 * t - 1; i++)
                keys[i] = new Key();
        }

        private int find(String k) {
            for (int i = 0; i < this.n; i++) {
                if (this.keys[i].key.equals(k)) {
                    return i;
                }
            }
            return -1;
        }
    }
    private class Key implements Serializable {
        String key;
        List<String> paths = new ArrayList<>();

        @Override
        public String toString() {
            return key + ": " + paths.toString();
        }

        @Override
        protected Object clone() {
            Key newKey = new Key();
            newKey.key = key;
            newKey.paths.addAll(paths);
            return newKey;
        }
    }

    public BTree(int t) {
        this.t = t;
        root = new Node();
        root.n = 0;
        root.leaf = true;
    }

    private Node root;

    private Node search(Node x, String key) {
        int i = 0;
        if (x == null)
            return x;
        for (i = 0; i < x.n; i++) {
            if (key.compareTo(x.keys[i].key) < 0) {
                break;
            }
            if (key.equals(x.keys[i].key)) {
                return x;
            }
        }
        if (x.leaf) {
            return null;
        } else {
            return search(x.children[i], key);
        }
    }

    public List<String> search(String key) {
        Node n = search(root, key);
        if (n != null)
            return n.keys[n.find(key)].paths;
        return null;
    }


    private void split(Node x, int pos, Node y) {
        Node z = new Node();
        z.leaf = y.leaf;
        z.n = t - 1;
        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
        }
        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
            }
        }
        y.n = t - 1;
        for (int j = x.n; j >= pos + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[pos + 1] = z;

        for (int j = x.n - 1; j >= pos; j--) {
            x.keys[j + 1] = x.keys[j];
        }
        x.keys[pos] = y.keys[t - 1];
        x.n = x.n + 1;
    }

    public boolean insert(String key, String path) {
        try {
            Node node = search(root, key);
            if (node == null) {
                Node r = root;
                if (r.n == 2 * t - 1) {
                    Node s = new Node();
                    root = s;
                    s.leaf = false;
                    s.n = 0;
                    s.children[0] = r;
                    split(s, 0, r);

                    insert(s, key, path);
                } else {
                    insert(r, key, path);
                }
            } else {
                int i = node.find(key);
                node.keys[i].paths.add(path);
            }
        } catch (CloneNotSupportedException e) {
            return false;
        }
        return true;
    }

    private void insert(Node x, String key, String path) throws CloneNotSupportedException {

        if (x.leaf) {
            int i = 0;
            for (i = x.n - 1; i >= 0 && key.compareTo(x.keys[i].key) < 0; i--) {
                x.keys[i + 1] = (Key) x.keys[i].clone();
            }
            x.keys[i + 1].key = key;
            x.keys[i + 1].paths.clear();
            x.keys[i + 1].paths.add(path);
            x.n = x.n + 1;
        } else {
            int i;
            for (i = x.n - 1; i >= 0 && key.compareTo(x.keys[i].key) < 0; i--) {}
            i++;
            Node tmp = x.children[i];
            if (tmp.n == 2 * t - 1) {
                split(x, i, tmp);
                if (key.compareTo(x.keys[i].key) > 0) {
                    i++;
                }
            }
            insert(x.children[i], key, path);
        }

    }

    public void show() {
        show(root);
        System.out.println();
    }

    private void remove(Node x, String key) throws CloneNotSupportedException {
        int pos = x.find(key);
        if (pos != -1) {
            if (x.leaf) {
                int i = 0;
                for (i = 0; i < x.n && !x.keys[i].key.equals(key); i++) {}
                for (; i < x.n; i++) {
                    if (i != 2 * t - 2) {
                        x.keys[i] = (Key) x.keys[i + 1].clone();
                    }
                }
                x.n--;
            } else {
                Node pred = x.children[pos];
                String predKey = "";
                if (pred.n >= t) {
                    for (;;) {
                        if (pred.leaf) {
                            System.out.println(pred.n);
                            predKey = pred.keys[pred.n - 1].key;
                            break;
                        } else {
                            pred = pred.children[pred.n];
                        }
                    }
                    remove(pred, predKey);
                    x.keys[pos].key = predKey;
                    return;
                }

                Node nextNode = x.children[pos + 1];
                if (nextNode.n >= t) {
                    String nextKey = nextNode.keys[0].key;
                    if (!nextNode.leaf) {
                        nextNode = nextNode.children[0];
                        for (;;) {
                            if (nextNode.leaf) {
                                nextKey = nextNode.keys[nextNode.n - 1].key;
                                break;
                            } else {
                                nextNode = nextNode.children[nextNode.n];
                            }
                        }
                    }
                    x.keys[pos] = (Key) nextNode.keys[0].clone();
                    remove(nextNode, nextKey);
                    return;
                }

                int temp = pred.n + 1;
                pred.keys[pred.n++] = x.keys[pos];
                for (int i = 0, j = pred.n; i < nextNode.n; i++) {
                    pred.keys[j++] = nextNode.keys[i];
                    pred.n++;
                }
                for (int i = 0; i < nextNode.n + 1; i++) {
                    pred.children[temp++] = nextNode.children[i];
                }

                x.children[pos] = pred;
                for (int i = pos; i < x.n; i++) {
                    if (i != 2 * t - 2) {
                        x.keys[i] = (Key) x.keys[i + 1].clone();
                    }
                }
                for (int i = pos + 1; i < x.n + 1; i++) {
                    if (i != 2 * t - 1) {
                        x.children[i] = x.children[i + 1];
                    }
                }
                x.n--;
                if (x.n == 0) {
                    if (x == root) {
                        root = x.children[0];
                    }
                    x = x.children[0];
                }
                remove(pred, key);
                return;
            }
        } else {
            for (pos = 0; pos < x.n; pos++) {
                if (x.keys[pos].key.compareTo(key) > 0) {
                    break;
                }
            }
            Node tmp = x.children[pos];
            if (tmp.n >= t) {
                remove(tmp, key);
                return;
            }
            if (true) {
                Node nb = null;
                String devider = "";

                if (pos != x.n && x.children[pos + 1].n >= t) {
                    devider = x.keys[pos].key;
                    nb = x.children[pos + 1];
                    x.keys[pos] = (Key) nb.keys[0].clone();
                    tmp.keys[tmp.n++].key = devider;
                    tmp.children[tmp.n] = nb.children[0];
                    for (int i = 1; i < nb.n; i++) {
                        nb.keys[i - 1] = (Key) nb.keys[i].clone();
                    }
                    for (int i = 1; i <= nb.n; i++) {
                        nb.children[i - 1] = nb.children[i];
                    }
                    nb.n--;
                    remove(tmp, key);
                    return;
                } else if (pos != 0 && x.children[pos - 1].n >= t) {

                    devider = x.keys[pos - 1].key;
                    nb = x.children[pos - 1];
                    x.keys[pos - 1] = (Key) nb.keys[nb.n - 1].clone();
                    Node child = nb.children[nb.n];
                    nb.n--;

                    for (int i = tmp.n; i > 0; i--) {
                        tmp.keys[i] = (Key) tmp.keys[i - 1].clone();
                    }
                    tmp.keys[0].key = devider;
                    for (int i = tmp.n + 1; i > 0; i--) {
                        tmp.children[i] = tmp.children[i - 1];
                    }
                    tmp.children[0] = child;
                    tmp.n++;
                    remove(tmp, key);
                    return;
                } else {
                    Node lt = null;
                    Node rt = null;
                    boolean last = false;
                    if (pos != x.n) {
                        devider = x.keys[pos].key;
                        lt = x.children[pos];
                        rt = x.children[pos + 1];
                    } else {
                        devider = x.keys[pos - 1].key;
                        rt = x.children[pos];
                        lt = x.children[pos - 1];
                        last = true;
                        pos--;
                    }
                    for (int i = pos; i < x.n - 1; i++) {
                        x.keys[i] = (Key) x.keys[i + 1].clone();
                    }
                    for (int i = pos + 1; i < x.n; i++) {
                        x.children[i] = x.children[i + 1];
                    }
                    x.n--;
                    lt.keys[lt.n++].key = devider;

                    for (int i = 0, j = lt.n; i < rt.n + 1; i++, j++) {
                        if (i < rt.n) {
                            lt.keys[j] = (Key) rt.keys[i].clone();
                        }
                        lt.children[j] = rt.children[i];
                    }
                    lt.n += rt.n;
                    if (x.n == 0) {
                        if (x == root) {
                            root = x.children[0];
                        }
                        x = x.children[0];
                    }
                    remove(lt, key);
                    return;
                }
            }
        }
    }

    public boolean remove(String key, String path) {
        try {
            Node x = search(root, key);
            if (x == null) {
                return false;
            } else {
                int i = x.find(key);
                if (x.keys[i].paths.size() <= 1)
                    remove(root, key);
                else
                    x.keys[i].paths.remove(path);
            }
        } catch (CloneNotSupportedException e) {
            return false;
        }
        return true;
    }

    // Show the node
    private void show(Node x) {
        assert (x == null);
        for (int i = 0; i < x.n; i++) {
            System.out.print(x.keys[i] + " ");
        }
        if (!x.leaf) {
            for (int i = 0; i < x.n + 1; i++) {
                show(x.children[i]);
            }
        }
    }
}