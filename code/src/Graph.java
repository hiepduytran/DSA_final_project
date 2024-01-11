import java.util.*;
import java.awt.*;

// Lớp biểu diễn đồ thị và triển khai thuật toán A*
public class Graph {
    // Priority queue cho các nút đã xét (closed set)
    private final PriorityQueue<Node> close;
    // Priority queue cho các nút chưa xét (open set)
    private final PriorityQueue<Node> open;
    // Map lưu trữ tất cả các nút trong đồ thị
    private final HashMap<Integer, Node> nodes;

    // Biến static để có thể truy cập đồ thị từ bất kỳ đâu
    public static Graph ins;

    // Constructor để khởi tạo đồ thị
    public Graph() {
        nodes = new HashMap<>();
        close = new PriorityQueue<>();
        open = new PriorityQueue<>();
        ins = this; // Gán đối tượng đồ thị cho biến static
    }

    // Phương thức trả về nút với ID cụ thể
    public Node getNode(int id) {
        if (nodes.containsKey(id)) {
            return nodes.get(id);
        } else {
            return null;
        }
    }

    // Phương thức thêm một nút vào đồ thị
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    // Phương thức thêm một cạnh kết nối giữa hai nút với khoảng cách tương ứng
    public void addEdge(int nodeId1, int nodeId2, double distance) {
        if (nodes.containsKey(nodeId1) && nodes.containsKey(nodeId2)) {
            nodes.get(nodeId1).addEdge(nodes.get(nodeId2), distance);
            nodes.get(nodeId2).addEdge(nodes.get(nodeId1), distance);
        }
    }

    // Phương thức xóa một cạnh kết nối giữa hai nút
    public void removeEdge(int nodeId1, int nodeId2) {
        if (nodes.containsKey(nodeId1) && nodes.containsKey(nodeId2)) {
            nodes.get(nodeId1).removeEdge(nodes.get(nodeId2));
            nodes.get(nodeId2).removeEdge(nodes.get(nodeId1));
        }
    }

    // Phương thức xóa một nút khỏi đồ thị
    public void removeNode(int nodeId) {
        if (nodes.containsKey(nodeId)) {
            Node temp = nodes.get(nodeId);
            for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
                entry.getValue().removeEdge(temp);
            }
        }
        nodes.remove(nodeId);
    }

    // Danh sách lưu trữ nút láng giềng
    public LinkedList<Node> nei = new LinkedList<>();

    // Phương thức triển khai thuật toán A*
    public double aStar(int startNodeId, int targetNodeId) {
        /*
        clear close và open, cũng như g và f của mọi node,
        trước khi triển khai thuật toán (phòng trường hợp còn kết quả của lần chạy trước)
        */
        // Xóa danh sách closed và open, cũng như g và f của mọi nút trước khi triển khai thuật toán
        close.clear();
        open.clear();
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            entry.getValue().g = 0;
            entry.getValue().f = 0;
        }
        // Vì nhập ID của node chứ không phải node, nên phải check xem có ID trong tập node của graph hay không?
        // Kiểm tra xem ID của nút bắt đầu và nút đích có trong đồ thị hay không
        if (nodes.containsKey(startNodeId) && nodes.containsKey(targetNodeId)) {
            Node startNode = nodes.get(startNodeId);
            Node targetNode = nodes.get(targetNodeId);
            startNode.g = 0;
            // Tính toán h của start đến target
            startNode.f = startNode.calculateHeuristic(targetNode);
            // Add target vào open
            open.add(startNode);
            // Kiểm tra open có trống hay không, thuật toán sẽ dừng nếu open trống và trả về "Không tìm được đường đi"
            // Duyệt qua tất cả các nút trong open set
            while (!open.isEmpty()) {
                // peek là lấy ra phần tử ưu tiên đầu tiên của priority queue (không xóa)
                Node currentNode = open.peek();

                // Check xem currentNode có phải targetNode không, nếu có thì thuật toán thành công và dừng
//              // (Kiểm tra xem nút hiện tại có phải là nút đích không)
                if (currentNode == targetNode) {
                    return currentNode.g;
                }

                // for này là duyệt qua toàn bộ neighbor của currentNode
                // Duyệt qua tất cả các nút láng giềng của nút hiện tại
                for (Map.Entry<Node, Double> neighborEntry : currentNode.getNeighbors().entrySet()) {
                    Node neighbor = neighborEntry.getKey();
                    nei.add(neighbor);

                    // Tính toán g của neighbor
                    // Tính toán chi phí mới từ nút bắt đầu đến nút láng giềng
                    double totalWeight = currentNode.g + neighborEntry.getValue();
                    // Check xem nếu neighbor chưa có trong open và close thì add vào open, set g với f
                    // Nếu nút láng giềng không có trong open và close set, thêm vào open và cập nhật chi phí
                    if (!open.contains(neighbor) && !close.contains(neighbor)) {
                        neighbor.g = totalWeight;
                        neighbor.f = totalWeight + neighbor.calculateHeuristic(targetNode);
                        // Set previous step cho neigbor thành current node
                        neighbor.setParent(currentNode);
                        open.add(neighbor);

                        // Đây là trường hợp neighbor đã tồn tại trong open hoặc close
                        // Nếu nút láng giềng đã có trong open hoặc close set, kiểm tra và cập nhật chi phí nếu cần
                    } else {
                        /*
                        Khi đã tồn tại trong open hoặc close, nghĩa là đã có đường đi đến neighbor trước
                        nên ta check xem đường mới này có ngắn hơn đường cũ không (bằng cách so sánh g cũ và g mới (total weight))
                        */
                        if (totalWeight < neighbor.g) {
                            neighbor.g = totalWeight;
                            neighbor.f = totalWeight + neighbor.calculateHeuristic(targetNode);
                            // Nếu đường mới đi đến neighbor ngắn hơn, và nếu trong open đã có sẵn thì nó
                            // vẫn trong open, rồi tự update lại vị trí trong priority queue, còn nếu
                            // trong close phải bỏ ra open để xét lại.
                            if (close.contains(neighbor)) {
                                open.add(neighbor);
                                close.remove(neighbor);
                            }
                            // Set previous step cho neighbor thành current node
                            neighbor.setParent(currentNode);
                        }
                    }
                }
                // Sau khi duyệt xong hết neighbor thì cho current node vào close, xóa ở open
                open.remove(currentNode);
                close.add(currentNode);
            }
        }
        return -1;
    }

    // Phương thức trả về danh sách các nút tạo thành đường đi
    public LinkedList<Node> getPath(int targetId) {
        LinkedList<Node> p = new LinkedList<>();
        Node point = nodes.get(targetId);

        // Duyệt qua danh sách nút cha để tạo thành đường đi
        while (point != null) {
            p.addFirst(point);
            point = point.getParent();
        }
        return p;
    }

    // Phương thức trả về đường đi dưới dạng chuỗi các ID của nút
    public String printPath(int targetId) {
        StringBuilder pathStr = new StringBuilder();
        for (Node point : getPath(targetId)) {
            pathStr.append(" " + point.getId());
            point = point.getParent();
        }
        return String.valueOf(pathStr);
    }
}
