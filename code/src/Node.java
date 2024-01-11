import java.util.HashMap;

// Lớp biểu diễn một nút trong đồ thị
public class Node implements Comparable<Node> {
    private double x;
    private double y;
    public double f = Double.MAX_VALUE;  // Chi phí tổng từ đầu đến nút hiện tại thông qua đường đi tốt nhất
    public double g = Double.MAX_VALUE;  // Chi phí từ đầu đến nút hiện tại
    public Node parent;  // Nút cha trong đường đi tốt nhất

    private static int idAutoIncreasing = 0;
    private int id;  // ID của node
    private HashMap<Node, Double> neighbors;  // Danh sách lưu trữ node hàng xóm và khoảng cách tương ứng

    // Phương thức trả về nút cha
    public Node getParent() {
        return parent;
    }

    // Phương thức thiết lập nút cha
    public void setParent(Node parent) {
        this.parent = parent;
    }

    // Constructor tạo mới một đối tượng Node với tọa độ x và y
    public Node(double x, double y) {
        this.id = idAutoIncreasing++;
        neighbors = new HashMap<>();
        this.x = x;
        this.y = y;
        parent = null;
    }

    // Phương thức thêm một cạnh kết nối giữa node hiện tại và node đích với khoảng cách tương ứng
    public void addEdge(Node target, double distance) {
        if (distance > 0) {
            neighbors.put(target, distance);
        }
    }

    // Phương thức xóa một cạnh kết nối giữa node hiện tại và node đích
    public void removeEdge(Node target) {
        neighbors.remove(target);
    }

    // Phương thức tính hàm heuristic (ước lượng chi phí từ node hiện tại đến node đích)
    public double calculateHeuristic(Node target) {
        return Math.sqrt(Math.pow(this.x - target.x, 2.0) + Math.pow(this.y - target.y, 2.0));
    }

    // Các phương thức getter và setter cho các thuộc tính
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<Node, Double> getNeighbors() {
        return neighbors;
    }

    // Phương thức so sánh hai đối tượng Node dựa trên giá trị của f
    @Override
    public int compareTo(Node node) {
        return Double.compare(this.f, node.f);
    }
}
