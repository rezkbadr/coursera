import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.ArrayList;
import java.util.List;

public class KdTree {

    private static final boolean X = true;
    private Node root;
    private int size;

    public KdTree() {
    }

    /*
     *********************************************************************
     ********************** KDTREE IMPLEMENTATION ************************
     *********************************************************************
     */

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public boolean contains(Point2D p) {
        if (p == null || isEmpty()) return false;
        Node node = traverse(p);
        return node.getVal() != null;
    }

    public void insert(Point2D p) {
        if (p == null) throw new IllegalArgumentException("Null point");
        if (contains(p)) return;
        if (root == null) {
            root = new Node(p);
            size++;
            return;
        }
        Node result = traverse(p);
        result.setVal(p);
        size++;
    }

    private Node traverse(Point2D point) {
        return traverse(point, root, X);
    }

    private Node traverse(Point2D point, Node currentNode, boolean axis) {
        int smaller = comparePointWithNode(point, currentNode, axis);
        if (point.x() == currentNode.getVal().x() && point.y() == currentNode.getVal().y())
            return currentNode;
        if (smaller < 0)
            return goLeft(point, currentNode, currentNode.getLeft(), !axis);
        return goRight(point, currentNode, currentNode.getRight(), !axis);
    }

    private int comparePointWithNode(Point2D point, Node node, boolean axis) {
        return axis == X ? Double.compare(point.x(), node.getVal().x()) :
               Double.compare(point.y(), node.getVal().y());
    }

    private Node goLeft(Point2D point, Node parent, Node node, boolean axis) {
        if (node == null || node.getVal() == null) {
            Node newNode = new Node();
            parent.setLeft(newNode);
            return newNode;
        }
        return traverse(point, node, axis);
    }

    private Node goRight(Point2D point, Node parent, Node node, boolean axis) {
        if (node == null || node.getVal() == null) {
            Node newNode = new Node();
            parent.setRight(newNode);
            return newNode;
        }
        return traverse(point, node, axis);
    }

    public void draw() {
        StdDraw.enableDoubleBuffering();
        StdDraw.clear();
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.5);
        drawNodes(root);
    }

    private void drawNodes(Node node) {
        if (node == null || node.getVal() == null) return;
        drawNodes(node.getLeft());
        node.getVal().draw();
        drawNodes(node.getRight());
    }

    public Iterable<Point2D> range(RectHV rect) {
        List<Point2D> result = new ArrayList<>();
        searchNearest(rect, root, X, result);
        return result;
    }

    private void searchNearest(RectHV rect, Node node, boolean axis, List<Point2D> result) {
        if (node == null || node.getVal() == null) return;
        addIfEligible(rect, node, result);

        if (axis == X) {
            if (rect.xmax() < node.getVal().x() && rect.xmin() <= node.getVal().x())
                searchNearest(rect, node.getLeft(), !axis, result);
            else if (rect.xmax() >= node.getVal().x() && rect.xmin() >= node.getVal().x())
                searchNearest(rect, node.getRight(), !axis, result);
            else {
                searchNearest(rect, node.getLeft(), !axis, result);
                searchNearest(rect, node.getRight(), !axis, result);
            }
        }
        else {
            if (rect.ymax() < node.getVal().y() && rect.ymin() <= node.getVal().y())
                searchNearest(rect, node.getLeft(), !axis, result);
            else if (rect.ymax() >= node.getVal().y() && rect.ymin() >= node.getVal().y())
                searchNearest(rect, node.getRight(), !axis, result);
            else {
                searchNearest(rect, node.getLeft(), !axis, result);
                searchNearest(rect, node.getRight(), !axis, result);
            }
        }
    }

    private void addIfEligible(RectHV rect, Node node, List<Point2D> result) {
        if (node.getVal().x() >= rect.xmin()
                && node.getVal().x() <= rect.xmax()
                && node.getVal().y() >= rect.ymin()
                && node.getVal().y() <= rect.ymax()) {
            result.add(node.getVal());
        }
    }

    public Point2D nearestNeighbor(Point2D query) {
        return nearestNeighbor(query, root, new Point2D(Integer.MAX_VALUE, Integer.MAX_VALUE), X);
    }

    private Point2D nearestNeighbor(Point2D query, Node node, Point2D champion, boolean axis) {
        if (node == null) return champion;

        Node nearestChild = nearestChild(node, query, axis);
        // if new champion, prune the other subtree
        if (node.getVal().distanceTo(query) < champion.distanceTo(query)) {
            return nearestNeighbor(query, nearestChild, node.getVal(), !axis);
        }
        else {
            // if not champion, then we might have to search both subtrees
            Point2D nearestResult = nearestNeighbor(query, nearestChild, node.getVal(), !axis);
            if (nearestResult.distanceTo(query) < champion.distanceTo(query)) {
                return nearestNeighbor(query, nearestChild, nearestResult, !axis);
            }
            else {
                // if rectangle containing the node is closer to the query than the champion, search it
                if (isRecCloserToQuery(query, node, champion, !axis))
                    return nearestNeighbor(query,
                                           nearestChild.equals(node.getLeft()) ? node.getLeft() :
                                           node.getRight(), champion, !axis);
            }
        }
        return champion;
    }

    private boolean isRecCloserToQuery(Point2D query, Node node, Point2D champion, boolean axis) {
        return axis == X ?
               Math.abs(query.x() - node.getVal().x()) < Math.abs(query.x() - champion.x()) :
               Math.abs(query.y() - node.getVal().y()) < Math.abs(query.y() - champion.y());
    }

    private Node nearestChild(Node node, Point2D query, boolean axis) {
        return axis == X ? query.x() < node.getVal().x() ? node.getLeft() : node.getRight()
                         : query.y() < node.getVal().y() ? node.getLeft() : node.getRight();
    }


    /*
     *********************************************************************
     ********************** NODE CLASS ***********************************
     *********************************************************************
     */


    private static class Node {
        private Node left;
        private Node right;
        private Point2D val;
        private boolean axis;

        public Node() {

        }

        public Node(Point2D val) {
            this.val = val;
        }

        public boolean isAxis() {
            return axis;
        }

        public void setAxis(boolean axis) {
            this.axis = axis;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public Point2D getVal() {
            return val;
        }

        public void setVal(Point2D val) {
            this.val = val;
        }
    }
}
