import { Link, Outlet } from "react-router-dom";
import "./route.css";

export default function Root() {
    return (
        <>
            <div id={"wrapper"}>
                <div id="sidebar">
                    <h1>Demos side bar</h1>
                    <nav>
                        <ul>
                            <li>
                                <Link to="/">Home</Link>
                            </li>
                            <li>
                                <a href={`/contacts/2`}>React Router Demo</a>
                            </li>
                            <li>
                                <a href={`/ml/demos/2x2`}>2x2 Network Demo</a>
                            </li>
                            <li>
                                <a href={`/ml/demos/new`}>New Demo</a>
                            </li>
                        </ul>
                    </nav>
                </div>
                <div id="detail">
                    <Outlet />
                </div>
            </div>
        </>
    );
}
