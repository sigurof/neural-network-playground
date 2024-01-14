import { Link, Outlet } from "react-router-dom";
import "./route.css";
import { ErrorPage } from "../ErrorPage.tsx";
import Contact from "./Contact.tsx";
import { Demo2x2RedBlue } from "../demos/2x2/Demo2x2RedBlue.tsx";
import { Demo2x3x2RedAndBlue } from "../demos/2x3x2/Demo2x3x2RedAndBlue.tsx";
import React from "react";
import { Demo as Demo4Layers } from "../demos/2x3x3x3/Demo.tsx";
import {Demo as DemoDynamicShader} from "../demos/dynamic-shader/Demo.tsx";

const path22 = "/ml/demos/22";
const path232 = `/ml/demos/232`;
const path4Layers = "/ml/demos/2333";
const pathDynamicShader = "/ml/demos/dynamic-demo"
export const routes = [
    {
        path: "/",
        element: <Root />,
        errorElement: <ErrorPage />,
        children: [
            {
                path: "/contacts/:contactId",
                element: <Contact />,
            },
            {
                path: path22,
                element: <Demo2x2RedBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: path232,
                element: <Demo2x3x2RedAndBlue />,
                errorElement: <ErrorPage />,
            },
            {
                path: path4Layers,
                element: <Demo4Layers />,
                errorElement: <ErrorPage />,
            },
            {
                path: pathDynamicShader,
                element: <DemoDynamicShader />,
                errorElement: <ErrorPage />,
            },
        ],
    },
];
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
                                <Link to={`/contacts/2`}>
                                    React Router Demo
                                </Link>
                            </li>
                            <li>
                                <Link to={path22}>2x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={path232}>2x3x2 Red/Blue Demo</Link>
                            </li>
                            <li>
                                <Link to={path4Layers}>
                                    2x3x3x3 Red/Green/Blue Demo
                                </Link>
                            </li>
                            <li>
                                <Link to={pathDynamicShader}>
                                    Dynamic 2x3x2 Red/Blue Demo
                                </Link>
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
