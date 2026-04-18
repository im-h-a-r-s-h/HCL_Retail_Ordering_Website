import { useEffect, useState } from "react";

const presetUsers = {
  admin: { username: "admin", password: "admin123", label: "Admin" },
  user: { username: "user", password: "user123", label: "User" }
};

const categories = ["ALL", "PIZZA", "COLD_DRINK", "BREAD"];

const initialAuth = (() => {
  const saved = localStorage.getItem("pizza-ordering-auth");
  if (!saved) {
    return { username: "user", password: "user123", role: "user" };
  }

  try {
    return JSON.parse(saved);
  } catch {
    return { username: "user", password: "user123", role: "user" };
  }
})();

function App() {
  const [auth, setAuth] = useState(initialAuth);
  const [selectedCategory, setSelectedCategory] = useState("ALL");
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState(null);
  const [orders, setOrders] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState({
    products: false,
    cart: false,
    orders: false,
    action: false
  });
  const [orderForm, setOrderForm] = useState({
    customerEmail: "test@example.com",
    couponCode: "SAVE10"
  });
  const [adminForm, setAdminForm] = useState({
    name: "",
    description: "",
    price: "",
    stockQuantity: "",
    category: "PIZZA"
  });

  useEffect(() => {
    localStorage.setItem("pizza-ordering-auth", JSON.stringify(auth));
  }, [auth]);

  useEffect(() => {
    loadProducts(selectedCategory);
  }, [selectedCategory, auth]);

  useEffect(() => {
    if (auth.role === "user" || auth.role === "admin") {
      loadCart();
      loadOrders();
    }
  }, [auth]);

  async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set("Content-Type", "application/json");
    headers.set(
      "Authorization",
      `Basic ${btoa(`${auth.username}:${auth.password}`)}`
    );

    const response = await fetch(path, {
      ...options,
      headers
    });

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
      const detail =
        data?.details?.join(", ") ||
        data?.message ||
        `Request failed with status ${response.status}`;
      throw new Error(detail);
    }

    return data;
  }

  async function loadProducts(category = "ALL") {
    setLoading((current) => ({ ...current, products: true }));
    setError("");
    try {
      const suffix = category === "ALL" ? "" : `?category=${category}`;
      const data = await apiFetch(`/api/products${suffix}`, { method: "GET" });
      setProducts(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, products: false }));
    }
  }

  async function loadCart() {
    setLoading((current) => ({ ...current, cart: true }));
    try {
      const data = await apiFetch("/api/cart/user/1", { method: "GET" });
      setCart(data);
    } catch (err) {
      setCart(null);
      if (!err.message.toLowerCase().includes("cart not found")) {
        setError(err.message);
      }
    } finally {
      setLoading((current) => ({ ...current, cart: false }));
    }
  }

  async function loadOrders() {
    setLoading((current) => ({ ...current, orders: true }));
    try {
      const data = await apiFetch("/api/orders/user/1", { method: "GET" });
      setOrders(data);
    } catch (err) {
      setOrders([]);
      if (!err.message.toLowerCase().includes("order")) {
        setError(err.message);
      }
    } finally {
      setLoading((current) => ({ ...current, orders: false }));
    }
  }

  async function addToCart(productId) {
    setLoading((current) => ({ ...current, action: true }));
    setMessage("");
    setError("");
    try {
      const data = await apiFetch("/api/cart/items", {
        method: "POST",
        body: JSON.stringify({
          userId: 1,
          productId,
          quantity: 1
        })
      });
      setCart(data);
      setMessage("Item added to cart.");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, action: false }));
    }
  }

  async function updateCartItem(itemId, quantity) {
    if (!cart) {
      return;
    }

    setLoading((current) => ({ ...current, action: true }));
    setMessage("");
    setError("");
    try {
      const data = await apiFetch(`/api/cart/${cart.id}/items/${itemId}`, {
        method: "PATCH",
        body: JSON.stringify({ quantity })
      });
      setCart(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, action: false }));
    }
  }

  async function removeCartItem(itemId) {
    if (!cart) {
      return;
    }

    setLoading((current) => ({ ...current, action: true }));
    setError("");
    try {
      const data = await apiFetch(`/api/cart/${cart.id}/items/${itemId}`, {
        method: "DELETE"
      });
      setCart(data);
      setMessage("Item removed.");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, action: false }));
    }
  }

  async function placeOrder() {
    if (!cart?.id) {
      setError("Add items to cart first.");
      return;
    }

    setLoading((current) => ({ ...current, action: true }));
    setMessage("");
    setError("");
    try {
      await apiFetch("/api/orders/place", {
        method: "POST",
        body: JSON.stringify({
          cartId: cart.id,
          customerEmail: orderForm.customerEmail,
          couponCode: orderForm.couponCode
        })
      });
      setMessage("Order placed successfully.");
      await Promise.all([loadCart(), loadOrders(), loadProducts(selectedCategory)]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, action: false }));
    }
  }

  async function reorder(orderId) {
    setLoading((current) => ({ ...current, action: true }));
    setMessage("");
    setError("");
    try {
      await apiFetch(`/api/orders/${orderId}/reorder`, { method: "POST" });
      setMessage("Reorder placed successfully.");
      await Promise.all([loadCart(), loadOrders(), loadProducts(selectedCategory)]);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, action: false }));
    }
  }

  async function createProduct(event) {
    event.preventDefault();
    setLoading((current) => ({ ...current, action: true }));
    setMessage("");
    setError("");
    try {
      await apiFetch("/api/products", {
        method: "POST",
        body: JSON.stringify({
          name: adminForm.name,
          description: adminForm.description,
          price: Number(adminForm.price),
          stockQuantity: Number(adminForm.stockQuantity),
          category: adminForm.category
        })
      });
      setAdminForm({
        name: "",
        description: "",
        price: "",
        stockQuantity: "",
        category: "PIZZA"
      });
      setMessage("Product created.");
      await loadProducts(selectedCategory);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading((current) => ({ ...current, action: false }));
    }
  }

  function switchUser(roleKey) {
    const preset = presetUsers[roleKey];
    setAuth({
      username: preset.username,
      password: preset.password,
      role: roleKey
    });
    setMessage(`${preset.label} mode active.`);
    setError("");
  }

  const totalItems =
    cart?.items?.reduce((count, item) => count + item.quantity, 0) ?? 0;

  return (
    <div className="app-shell">
      <div className="backdrop backdrop-one" />
      <div className="backdrop backdrop-two" />

      <header className="hero">
        <div>
          <p className="eyebrow">Retail Ordering Website</p>
          <h1>Pizza, cold drinks, and breads in one quick demo flow.</h1>
          <p className="hero-copy">
            Browse the menu, filter by category, add items to cart, place an
            order, apply offers, and review order history without touching the
            backend.
          </p>
        </div>

        <section className="auth-card panel">
          <div className="panel-title-row">
            <h2>Session</h2>
            <span className="status-chip">{auth.role.toUpperCase()}</span>
          </div>
          <div className="auth-grid">
            <button type="button" className="secondary-button" onClick={() => switchUser("user")}>
              Use User
            </button>
            <button type="button" className="secondary-button" onClick={() => switchUser("admin")}>
              Use Admin
            </button>
          </div>
          <label>
            Username
            <input
              value={auth.username}
              onChange={(event) =>
                setAuth((current) => ({ ...current, username: event.target.value }))
              }
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={auth.password}
              onChange={(event) =>
                setAuth((current) => ({ ...current, password: event.target.value }))
              }
            />
          </label>
          <p className="helper-text">
            Frontend sends Basic Auth with every request. Keep the backend
            running on port 8081.
          </p>
        </section>
      </header>

      {message ? <div className="banner banner-success">{message}</div> : null}
      {error ? <div className="banner banner-error">{error}</div> : null}

      <main className="layout">
        <section className="catalog panel">
          <div className="panel-title-row">
            <div>
              <h2>Menu</h2>
              <p className="subtle">Customer enablement starts with simple browsing.</p>
            </div>
            <div className="meta-box">
              <span>{loading.products ? "Loading..." : `${products.length} items`}</span>
            </div>
          </div>

          <div className="category-row">
            {categories.map((category) => (
              <button
                key={category}
                type="button"
                className={category === selectedCategory ? "category-button active" : "category-button"}
                onClick={() => setSelectedCategory(category)}
              >
                {category === "ALL" ? "All Menu" : category.replace("_", " ")}
              </button>
            ))}
          </div>

          <div className="product-grid">
            {products.map((product) => (
              <article key={product.id} className="product-card">
                <div className="product-header">
                  <span className="category-pill">{product.category.replace("_", " ")}</span>
                  <span className="stock-pill">{product.stockQuantity} left</span>
                </div>
                <h3>{product.name}</h3>
                <p>{product.description}</p>
                <div className="price-row">
                  <strong>Rs. {product.price}</strong>
                  <button type="button" className="primary-button" onClick={() => addToCart(product.id)}>
                    Add
                  </button>
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside className="side-stack">
          <section className="panel">
            <div className="panel-title-row">
              <div>
                <h2>Cart</h2>
                <p className="subtle">{totalItems} item(s)</p>
              </div>
              <span className="meta-box">{loading.cart ? "Refreshing..." : `Rs. ${cart?.totalAmount ?? 0}`}</span>
            </div>

            {!cart?.items?.length ? (
              <p className="empty-state">Cart is empty. Add a product to start the order flow.</p>
            ) : (
              <div className="stack-list">
                {cart.items.map((item) => (
                  <div key={item.id} className="cart-item">
                    <div>
                      <strong>{item.productName}</strong>
                      <p>
                        {item.quantity} x Rs. {item.unitPrice}
                      </p>
                    </div>
                    <div className="cart-actions">
                      <button type="button" className="tiny-button" onClick={() => updateCartItem(item.id, item.quantity + 1)}>
                        +
                      </button>
                      <button
                        type="button"
                        className="tiny-button"
                        onClick={() => updateCartItem(item.id, Math.max(1, item.quantity - 1))}
                      >
                        -
                      </button>
                      <button type="button" className="tiny-button danger" onClick={() => removeCartItem(item.id)}>
                        x
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="order-form">
              <label>
                Customer Email
                <input
                  value={orderForm.customerEmail}
                  onChange={(event) =>
                    setOrderForm((current) => ({ ...current, customerEmail: event.target.value }))
                  }
                />
              </label>
              <label>
                Coupon Code
                <input
                  value={orderForm.couponCode}
                  onChange={(event) =>
                    setOrderForm((current) => ({ ...current, couponCode: event.target.value }))
                  }
                />
              </label>
              <button type="button" className="primary-button wide" onClick={placeOrder}>
                Place Order
              </button>
            </div>
          </section>

          <section className="panel">
            <div className="panel-title-row">
              <div>
                <h2>Order History</h2>
                <p className="subtle">Quick reorder is available here.</p>
              </div>
              <span className="meta-box">{loading.orders ? "Loading..." : orders.length}</span>
            </div>

            {!orders.length ? (
              <p className="empty-state">No orders yet.</p>
            ) : (
              <div className="stack-list">
                {orders.map((order) => (
                  <div key={order.id} className="order-card">
                    <div className="order-top">
                      <strong>Order #{order.id}</strong>
                      <span className="status-chip confirmed">{order.status}</span>
                    </div>
                    <p>{order.customerEmail || "No email provided"}</p>
                    <p>Discount: Rs. {order.discountAmount}</p>
                    <p>Loyalty Points: {order.loyaltyPointsEarned}</p>
                    <p>Total: Rs. {order.totalAmount}</p>
                    <button type="button" className="secondary-button wide" onClick={() => reorder(order.id)}>
                      Reorder
                    </button>
                  </div>
                ))}
              </div>
            )}
          </section>

          {auth.role === "admin" ? (
            <section className="panel">
              <div className="panel-title-row">
                <div>
                  <h2>Admin Quick Add</h2>
                  <p className="subtle">Create menu items without using Swagger or Postman.</p>
                </div>
              </div>

              <form className="admin-form" onSubmit={createProduct}>
                <label>
                  Product Name
                  <input
                    value={adminForm.name}
                    onChange={(event) =>
                      setAdminForm((current) => ({ ...current, name: event.target.value }))
                    }
                  />
                </label>
                <label>
                  Description
                  <textarea
                    value={adminForm.description}
                    onChange={(event) =>
                      setAdminForm((current) => ({ ...current, description: event.target.value }))
                    }
                  />
                </label>
                <div className="two-column">
                  <label>
                    Price
                    <input
                      type="number"
                      value={adminForm.price}
                      onChange={(event) =>
                        setAdminForm((current) => ({ ...current, price: event.target.value }))
                      }
                    />
                  </label>
                  <label>
                    Stock
                    <input
                      type="number"
                      value={adminForm.stockQuantity}
                      onChange={(event) =>
                        setAdminForm((current) => ({ ...current, stockQuantity: event.target.value }))
                      }
                    />
                  </label>
                </div>
                <label>
                  Category
                  <select
                    value={adminForm.category}
                    onChange={(event) =>
                      setAdminForm((current) => ({ ...current, category: event.target.value }))
                    }
                  >
                    <option value="PIZZA">PIZZA</option>
                    <option value="COLD_DRINK">COLD_DRINK</option>
                    <option value="BREAD">BREAD</option>
                  </select>
                </label>
                <button type="submit" className="primary-button wide">
                  Create Product
                </button>
              </form>
            </section>
          ) : null}
        </aside>
      </main>

      <footer className="footer-note">
        <span>Backend API: Spring Boot on 8081</span>
        <span>Frontend: React + Vite proxy on 5173</span>
      </footer>
    </div>
  );
}

export default App;
